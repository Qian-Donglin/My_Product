#pragma once

#include <Siv3D.hpp>
#include "basement.h"

class InputAnswerScene : public App::Scene {

private:
	//4���̓��ɂ���a b c d
	Array<String> prefix;
	//4���̑I������ID
	Array<int> choices;
	int answerIdxInChoices;//����͓��{�ꂩ��p��̎��ɂ͎g��Ȃ��IgetData().nowTestedIdx���g�����ƁB
	//�I�΂ꂽ�ދ`��
	//4���̏ꍇ��4�A�X�y���ł����݂̏ꍇ��1��
	Array<String> synonymWord;

	//�ł��̃t�H���g, �\�L���錾�t�̃t�H���g�A�傫�߂ȕ\�L���錾�t�̃t�H���g�A�����́��������t�H���g
	Font typingFont, showingWordFont, showingLargeWordFont, showingCorrectSignFont;
	Font synonymFont, synonymFontLarge;//�ދ`��̃t�H���g�Ƃ���̑傫����

	//���͂��ꂽ�������String
	String inputText;

	String showingWord;

	//���͘g
	Rect typingArea;
	//choosing -> spell�̎��A�S�̑I���������̕s���Ȏl�p�`�̒��ɂ����
	Rect choicearea[4];
	Rect oneChoiceArea;
	Rect hintWordArea;

	ll pastTime;
	//�����������́A�Z��1�b�\�L����@true�Ȃ炻�̃��[�h
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
		//idx�Ԗڂ̒P��̗ދ`��������_����1�I�ԁB
		auto target = getData().targetWord[idx];

		//���������^�O���Ȃ��ꍇ
		if (target.tags.isEmpty()) {
			return U"�ދ`��Ȃ�";
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

		//sortlist�̒��ł���1�c���Ă���@�܂肱�̃^�O�͂��̌�݂̂����ꍇ�͗ދ`�ꂪ�Ȃ��B
		if (sortlist.size() == 1) {
			return U"�ދ`��Ȃ�";
		}

		int selected_idx = 0;
		do {
			selected_idx = Random((int)0, (int)sortlist.size() - 1);
		} while (sortlist[selected_idx] == idx);

		return getData().targetWord[sortlist[selected_idx]].spellLang;
	}

	void resetProblemScene() {
		choices.clear();

		//getData().nowTestedIdx��cdf_operated�ɏ]���ă����_���Ɍ��肷��B
		int randIncdf_operated = getData().cdf_operated.lower_bound(Random(getData().total_weight - 1)) - 1;
		if (randIncdf_operated < 0)randIncdf_operated = 0;

		getData().nowTestedIdx = randIncdf_operated;

		//���[�h�����肷��B
		int MeanToSpellTyping = std::any_cast<int>(getData().ConfigParams[getData().rateMeanToSpellTyping]);
		int MeanToSpellChoosing = std::any_cast<int>(getData().ConfigParams[getData().rateMeanToSpellChoosing]);
		int SpellToMeanChoosing = std::any_cast<int>(getData().ConfigParams[getData().rateSpellToMeanChoosing]);
		
		//sum�����3�̘a�Ƃ��āAMeanToSpellTyping : MeanToSpellChoosing : SpellToMeanChoosing �̔䗦�Ŋe���[�h�ɂȂ�B
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
			//�R�}���h���[�h�@�ł����񂾃R�}���h�ɏ]���Ă��낢��N����B
			//���A�����ł͓��ɂ�邱�Ƃ͂Ȃ��B
		}
		else if (getData().problemMode == 1 || getData().problemMode == 2) {
			//spell -> choosing�@�p�ꂩ����{��̎l��
			
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
				//�����������ɑ΂��Ĉ������B
				if (choices[i] == getData().nowTestedIdx)
					answerIdxInChoices = i;

				//���ꂼ��ɑ�������4�̌�ɑ΂��āA�ދ`���I�яo���B
				synonymWord[i] = getsynonymWord(choices[i]);
			}

		}
		else {
			//���{��̉������p��̃X�y���𓚂���
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
				//�R�}���h���[�h�@���͂����R�}���h�ɏ]���Ă����Ȃ��Ƃ��N����B

				if (KeyEnter.up()) {
					String target = inputText.substr(0, inputText.size() - 1);
					if (target == U"tweet") {
						//�����_�ł̕׋����c�C�[�g����B
						Twitter::OpenTweetWindow(getData().fillNumberToTweet(getData().tweetSentence));
						getData().didBeforeTweeted = true;
					}

					resetProblemScene();
				}
			}
			else if (getData().problemMode == 0) {

				//enter�œ��͊���
				if (KeyEnter.up()) {

					//enter�łǂ��������Ă��ĕʕ��Ƃ��Ĉ�����炵���BinputText�̖�����Enter�������čl����

					//�܂��R�}���h���[�h���ǂ������m�F�@�R�}���h���[�h��cmd��command�ňڍs�\�B
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
							//����ŏ��߂ďo���P�ꂾ������
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
							//�ڍׂȒP��ꗗScene��
							changeScene(U"ShowWordDetailScene", 0.2);
							endProblemScene();
							WrongAnswer(getData().nowTestedIdx);
						}
						else {
							//����
							duringOKScene = true;
							endProblemScene();
							CorrectAnswer(getData().nowTestedIdx);
						}
					}
					inputText.clear();
				}
			}
			else {
				//spellLang -> choosingLang or choosingLang -> SpellLang�̎l��
				//�����ł́A��̂����ꂩ�ł���A�Ƃ�������͍s���Ă��炸��̓I�ȕ`��͂��ׂ�draw()�ōs���Ă���B
				
				if (KeyEnter.up()) {

					//�܂��R�}���h���[�h���ǂ������m�F�@�R�}���h���[�h��cmd��command�ňڍs�\�B
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

		//1.0s�Ԋu�œ_�ł�����(����1.2s�̋�`�g��1�̎��̂݁���\��)
		typingArea.draw(Palette::Azure);
		typingFont(inputText + String(
			Periodic::Square0_1(1.2s) ? U"��" : U""
		)).draw(typingArea.stretched(-5), Palette::Black);

		showingWordFont(U"Total Solved: " + Format(getData().totalSolved)).draw(500, 10, Palette::Black);
		showingWordFont(U"Today Solved: " + Format(getData().todaySolved)).draw(500, 45, Palette::Black);
		showingWordFont(U"This try Solved: " + Format(getData().thisTrySolved)).draw(500, 80, Palette::Black);
		showingWordFont(U"This Try Solved Uniquely: " + Format(getData().uniqueSolvedCount)).draw(480, 115, Palette::Black);

		if (getData().problemMode == -1) {
			showingLargeWordFont(U"�R�}���h��t���").draw(30, 30, Palette::Black);
		}
		else if (getData().problemMode == 0) {
			//choosingLang -> spellLang

			showingLargeWordFont(
				getData().targetWord[getData().nowTestedIdx].choosingLang
			).draw(oneChoiceArea, Palette::Black);
			if (std::any_cast<int>(getData().ConfigParams[getData().doShowSynonym]) != 0) {
				//�ދ`��̕`��
				synonymFontLarge(U"syn: " + synonymWord[0])
					.draw(100, 300, Palette::Black);
			}

		}
		else {
			//ChoosingLang -> spellLang(1), spellLang -> ChoosingLang(2)
			//�l���őI������ꍇ
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
				//�I�����̕`��
				showingWordFont(
					prefix[i] + U". " + 
					(getData().problemMode == 1
						? getData().targetWord[choices[i]].spellLang
						: getData().targetWord[choices[i]].choosingLang)
				).draw(choicearea[i], Palette::Black);

				if (std::any_cast<int>(getData().ConfigParams[getData().doShowSynonym]) != 0) {
					//�ދ`��̕`��
					synonymFont(U"syn: " + synonymWord[i])
						.draw(550, 250 + 60 * i, Palette::Black);
				}
			}

		}

		if (duringOKScene) {
			showingCorrectSignFont(U"�Z").drawAt(400, 300, Palette::Green);
		}

	}

};