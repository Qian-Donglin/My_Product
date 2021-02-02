#pragma once

#include <Siv3D.hpp>
#include "basement.h"

class InputAnswerScene : public App::Scene {

private:
	//4択の頭につけるa b c d
	Array<String> prefix;
	//4択の選択肢のID
	Array<int> choices;
	int answerIdxInChoices;//これは日本語から英語の時には使わない！getData().nowTestedIdxを使うこと。
	//選ばれた類義語
	//4択の場合は4つ、スペル打ち込みの場合は1つ
	Array<String> synonymWord;

	//打つ字のフォント, 表記する言葉のフォント、大きめな表記する言葉のフォント、正解の○を示すフォント
	Font typingFont, showingWordFont, showingLargeWordFont, showingCorrectSignFont;
	Font synonymFont, synonymFontLarge;//類義語のフォントとそれの大きい版

	//入力された文字列のString
	String inputText;

	String showingWord;

	//入力枠
	Rect typingArea;
	//choosing -> spellの時、４つの選択肢をこの不可視な四角形の中にいれる
	Rect choicearea[4];
	Rect oneChoiceArea;
	Rect hintWordArea;

	ll pastTime;
	//正解した時は、〇を1秒表記する　trueならそのモード
	bool duringOKScene;

public:

	void CorrectAnswer(int idx) {
		int val = getData().targetWord[idx].Correct();
		getData().total_weight += -val - (int)((double)val * getData().adapt_forgetting_curve(idx));
		getData().cdf_operated.update(idx + 1, -val - (int)((double)val * getData().adapt_forgetting_curve(idx)));
	}

	void WrongAnswer(int idx) {
		int val = getData().targetWord[idx].Wrong();
		getData().total_weight += val + (int)((double)val * getData().adapt_forgetting_curve(idx));
		getData().cdf_operated.update(idx + 1, val + (int)((double)val * getData().adapt_forgetting_curve(idx)));
	}

	String getsynonymWord(int idx) {
		//idx番目の単語の類義語をランダムに1つ選ぶ。
		auto target = getData().targetWord[idx];

		//そもそもタグがない場合
		if (target.tags.isEmpty()) {
			return U"類義語なし";
		}

		String ret = U"";
		Array<int> sortlist;
		for (int i = 0; i < target.tags.size(); i++) {
			Array<int>& tmp = getData().tagsToWordsIdx[target.tags[i]];
			for (int j = 0; j < tmp.size(); j++) {
				sortlist.push_back(tmp[j]);
			}
		}
		std::sort(sortlist.begin(), sortlist.end());
		sortlist.erase(std::unique(sortlist.begin(), sortlist.end()), sortlist.end());

		//sortlistの中でただ1つ残っている　つまりこのタグはこの語のみが持つ場合は類義語がない。
		if (sortlist.size() == 1) {
			return U"類義語なし";
		}

		int selected_idx = 0;
		do {
			selected_idx = Random((int)0, (int)sortlist.size() - 1);
		} while (sortlist[selected_idx] == idx);

		return getData().targetWord[sortlist[selected_idx]].spellLang;
	}

	void resetProblemScene() {
		choices.clear();

		//getData().nowTestedIdxをcdf_operatedに従ってランダムに決定する。
		int randIncdf_operated = getData().cdf_operated.lower_bound(Random(getData().total_weight - 1)) - 1;
		if (randIncdf_operated < 0)randIncdf_operated = 0;

		getData().nowTestedIdx = randIncdf_operated;

		//モードを決定する。
		int MeanToSpellTyping = std::any_cast<int>(getData().ConfigParams[getData().rateMeanToSpellTyping]);
		int MeanToSpellChoosing = std::any_cast<int>(getData().ConfigParams[getData().rateMeanToSpellChoosing]);
		int SpellToMeanChoosing = std::any_cast<int>(getData().ConfigParams[getData().rateSpellToMeanChoosing]);
		
		//sumを上の3つの和として、MeanToSpellTyping : MeanToSpellChoosing : SpellToMeanChoosing の比率で各モードになる。
		int sum = std::any_cast<int>(getData().ConfigParams[getData().rateMeanToSpellTyping])
			+ std::any_cast<int>(getData().ConfigParams[getData().rateMeanToSpellChoosing])
			+ std::any_cast<int>(getData().ConfigParams[getData().rateSpellToMeanChoosing]);

		int valueForProblemMode = Random((int)0, (int)sum - 1);
		
		if (valueForProblemMode < MeanToSpellTyping)
			getData().problemMode = 0;
		else if (valueForProblemMode < MeanToSpellTyping + MeanToSpellChoosing)
			getData().problemMode = 1;
		else
			getData().problemMode = 2;

		inputText.clear();

		if (getData().problemMode == -1) {
			//コマンドモード　打ち込んだコマンドに従っていろいろ起きる。
			//が、ここでは特にやることはない。
		}
		else if (getData().problemMode == 1 || getData().problemMode == 2) {
			//spell -> choosing　英語から日本語の四択
			
			choices.push_back(getData().nowTestedIdx);
			int i = 1;

			while (i < 4) {
				int randomFakeChoice = Random(getData().targetWord.size() - 1);
				bool isok = true;
				for (int j = 0; j < i; j++)
					if (randomFakeChoice == choices[j])
						isok = false;
				if (isok) {
					choices.push_back(randomFakeChoice);
					i++;
				}
			}
			choices.shuffle();

			synonymWord.resize(4);
			for (i = 0; i < 4; i++) {
				//正しい答えに対して印をつける。
				if (choices[i] == getData().nowTestedIdx)
					answerIdxInChoices = i;

				//それぞれに相当する4つの語に対して、類義語を選び出す。
				synonymWord[i] = getsynonymWord(choices[i]);
			}

		}
		else {
			//日本語の解説から英語のスペルを答える
			synonymWord.resize(1);
			synonymWord[0] = getsynonymWord(getData().nowTestedIdx);
		}
	}

	void endProblemScene() {
		getData().memoedTimeMili = Time::GetMillisec();
	}

	InputAnswerScene(const InitData& init) : IScene(init) {
		/*
		typingFont = Font(25);
		showingWordFont = Font(20);
		showingLargeWordFont = Font(30);
		showingCorrectSignFont = Font(250);

		synonymFont = Font(15);
		synonymFontLarge = Font(20);
		*/
		
		//, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf"
		typingFont = Font(25, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
		showingWordFont = Font(20, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
		showingLargeWordFont = Font(30, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
		showingCorrectSignFont = Font(250, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");

		synonymFont = Font(15, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
		synonymFontLarge = Font(20, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
		

		typingArea = Rect(100, 500, 500, 50);
		for (int i = 0; i < 4; i++) {
			choicearea[i] = Rect(50, 250 + 60 * i, 500, 60);
		}
		oneChoiceArea = Rect(100, 150, 500, 350);
		hintWordArea = Rect(100, 100, 380, 250);

		pastTime = 0;

		getData().problemMode = RandomBool();

		prefix.push_back(U"a");
		prefix.push_back(U"b");
		prefix.push_back(U"c");
		prefix.push_back(U"d");

		getData().isCorrect = false, duringOKScene = false;
		resetProblemScene();
	}

	void update() override {

		switch (duringOKScene) {

		case true:
			if (Time::GetMillisec() - getData().memoedTimeMili >= 1000) {
				duringOKScene = false;
				resetProblemScene();
			}
			break;
		default:
			TextInput::UpdateText(inputText);

			if (getData().problemMode == -1) {
				//コマンドモード　入力したコマンドに従っていろんなことが起きる。

				if (KeyEnter.up()) {
					String target = inputText.substr(0, inputText.size() - 1);
					if (target == U"tweet") {
						//現時点での勉強をツイートする。
						Twitter::OpenTweetWindow(getData().fillNumberToTweet(getData().tweetSentence));
						getData().didBeforeTweeted = true;
					}

					resetProblemScene();
				}
			}
			else if (getData().problemMode == 0) {

				//enterで入力完了
				if (KeyEnter.up()) {

					//enterでどうやら入っていて別物として扱われるらしい。inputTextの末尾のEnterを消して考える

					//まずコマンドモードかどうかを確認　コマンドモードはcmdかcommandで移行可能。
					if (inputText.substr(0, inputText.size() - 1) == U"cmd"
						|| inputText.substr(0, inputText.size() - 1) == U"command") {
						getData().problemMode = -1;
					}
					else
					{
						getData().thisTrySolved++;
						getData().totalSolved++;
						getData().todaySolved++;

						if (inputText.substr(0, inputText.size() - 1) ==
							getData().targetWord[getData().nowTestedIdx].spellLang) {
							getData().isCorrect = true;
							//今回で初めて出た単語だったか
							if (!getData().isSolved[getData().targetWord[getData().nowTestedIdx].spellLang]) {
								getData().uniqueSolvedCount++;
								getData().isSolved[getData().targetWord[getData().nowTestedIdx].spellLang] = true;
							}
						}
						else {
							getData().isCorrect = false;
							WrongAnswer(getData().nowTestedIdx);
						}
						pastTime = Time::GetMillisec();


						if (!getData().isCorrect) {
							//詳細な単語一覧Sceneへ
							changeScene(U"ShowWordDetailScene", 0.2);
							endProblemScene();
							WrongAnswer(getData().nowTestedIdx);
						}
						else {
							//正解
							duringOKScene = true;
							endProblemScene();
							CorrectAnswer(getData().nowTestedIdx);
						}
					}
					inputText.clear();
				}
			}
			else {
				//spellLang -> choosingLang or choosingLang -> SpellLangの四択
				//ここでは、上のいずれかである、という分岐は行っておらず具体的な描画はすべてdraw()で行われている。
				
				if (KeyEnter.up()) {

					//まずコマンドモードかどうかを確認　コマンドモードはcmdかcommandで移行可能。
					if (inputText.substr(0, inputText.size() - 1) == U"cmd"
						|| inputText.substr(0, inputText.size() - 1) == U"command") {
						getData().problemMode = -1;
					}
					else
					{
						getData().thisTrySolved++;
						getData().totalSolved++;
						getData().todaySolved++;
						if (inputText.substr(0, inputText.size() - 1) == prefix[answerIdxInChoices]) {
							if (!getData().isSolved[getData().targetWord[getData().nowTestedIdx].spellLang]) {
								getData().uniqueSolvedCount++;
								getData().isSolved[getData().targetWord[getData().nowTestedIdx].spellLang] = true;
							}
							CorrectAnswer(getData().nowTestedIdx);
							endProblemScene();
							duringOKScene = true;
						}
						else {
							changeScene(U"ShowWordDetailScene", 0.2);
							WrongAnswer(getData().nowTestedIdx);
							endProblemScene();
						}
					}
					inputText.clear();
				}
			}
		}
	}

	void draw() const override {
		Scene::SetBackground(Color(230, 230, 230));

		//1.0s間隔で点滅させる(周期1.2sの矩形波で1の時のみ←を表示)
		typingArea.draw(Palette::Azure);
		typingFont(inputText + String(
			Periodic::Square0_1(1.2s) ? U"←" : U""
		)).draw(typingArea.stretched(-5), Palette::Black);

		showingWordFont(U"Total Solved: " + Format(getData().totalSolved)).draw(500, 10, Palette::Black);
		showingWordFont(U"Today Solved: " + Format(getData().todaySolved)).draw(500, 45, Palette::Black);
		showingWordFont(U"This try Solved: " + Format(getData().thisTrySolved)).draw(500, 80, Palette::Black);
		showingWordFont(U"This Try Solved Uniquely: " + Format(getData().uniqueSolvedCount)).draw(480, 115, Palette::Black);

		if (getData().problemMode == -1) {
			showingLargeWordFont(U"コマンド受付画面").draw(30, 30, Palette::Black);
		}
		else if (getData().problemMode == 0) {
			//choosingLang -> spellLang

			showingLargeWordFont(
				getData().targetWord[getData().nowTestedIdx].choosingLang
			).draw(oneChoiceArea, Palette::Black);
			if (std::any_cast<int>(getData().ConfigParams[getData().doShowSynonym]) != 0) {
				//類義語の描画
				synonymFontLarge(U"syn: " + synonymWord[0])
					.draw(100, 300, Palette::Black);
			}

		}
		else {
			//ChoosingLang -> spellLang(1), spellLang -> ChoosingLang(2)
			//四択で選択する場合
			if (getData().problemMode == 1) {
				showingWordFont(
					getData().targetWord[getData().nowTestedIdx].choosingLang
				).draw(hintWordArea, Palette::Black);
			}
			else {
				showingLargeWordFont(
					getData().targetWord[getData().nowTestedIdx].spellLang
				).draw(100, 100, Palette::Black);
			}

			for (int i = 0; i < 4; i++) {
				//選択肢の描画
				showingWordFont(
					prefix[i] + U". " + 
					(getData().problemMode == 1
						? getData().targetWord[choices[i]].spellLang
						: getData().targetWord[choices[i]].choosingLang)
				).draw(choicearea[i], Palette::Black);

				if (std::any_cast<int>(getData().ConfigParams[getData().doShowSynonym]) != 0) {
					//類義語の描画
					synonymFont(U"syn: " + synonymWord[i])
						.draw(550, 250 + 60 * i, Palette::Black);
				}
			}

		}

		if (duringOKScene) {
			showingCorrectSignFont(U"〇").drawAt(400, 300, Palette::Green);
		}

	}

};