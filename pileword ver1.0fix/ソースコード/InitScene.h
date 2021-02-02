#pragma once

#include <Siv3D.hpp>

#include"basement.h"

class InitScene : public App::Scene {
private:
    Font welcomeFont;

public:

    void setConfigurationFromFile(const CSVData& config) {
        
        getData().ConfigParams[getData().rateMeanToSpellChoosing] = 1;
        getData().ConfigParams[getData().rateMeanToSpellTyping] = 1;
        getData().ConfigParams[getData().rateSpellToMeanChoosing] = 1;
        
        for (int i = 0; i < config.rows(); i++) {
            String tmp = ParseOr<String>(config[i][0], U"");
            if (tmp == U"Spell To Meaning Choosing")
                getData().ConfigParams[getData().rateSpellToMeanChoosing] = ParseOr<int>(config[i][1], 1);
            if (tmp == U"Meaning To Spell Typing")
                getData().ConfigParams[getData().rateMeanToSpellTyping] = ParseOr<int>(config[i][1], 1);
            if (tmp == U"Meaning To Spell Choosing")
                getData().ConfigParams[getData().rateMeanToSpellChoosing] = ParseOr<int>(config[i][1], 1);
            if (tmp == U"Show Synonym")
                getData().ConfigParams[getData().doShowSynonym] = ParseOr<int>(config[i][1], 1);
                
        }
        
        //recentWordを可変にあとでする。

    }

    void readFile() {
        //WordData.csvとWordHistory.csvを開く　WordHistory.csvの行数が足りない場合は0埋めする。
        const CSVData csvWordData(U"WordData.csv");
        if (!csvWordData) {
            throw Error(U"WordData.csv doesn't exist.");
        }

        const CSVData csvWordHistory(U"WordHistory.csv");
        if (!csvWordHistory) {
            throw Error(U"WordHistory.csv doesn't exist.");
        }

        const CSVData csvConfiguration(U"Configuration.csv");
        if (!csvWordHistory) {
            throw Error(U"Configuration.csv doesn't exist.");
        }

        {
            //BITにcdf_operated(累積分布関数の二分探索)の役割を持たせる。
            std::vector<ll> tmp(csvWordData.rows() + 1, 0);
            getData().cdf_operated = BIT(tmp);
        }

        //Wordごとの意味や難易度係数、タグを読み込む部分
        int wordNumber = csvWordData.rows();
        for (int i = 0; i < wordNumber; i++) {
            //targetWordとcdf_operatedに入れる。

            //新規単語の重みは300としている。既存のがあれば上書きする。
            ll weight = 300;
            ll next_allowed = 0;

            //タグをタグから英単語インデックスの逆引きmapに登録しておく
            auto tags = splitString(csvWordData[i][getData().rowTag], U' ');
            for (int j = 0; j < tags.size(); j++) {
                getData().tagsToWordsIdx[tags[j]].push_back(i);
            }
            
            //すでに難易度係数が存在する場合は、それを参照する。
            if (i + 1 < csvWordHistory.rows()) {
                weight = ParseOr<int>(csvWordHistory[i + 1][getData().rowDifficulty], 0);
                next_allowed = ParseOr<long long>(csvWordHistory[i + 1][getData().rowBeforeOccurency], 0);
            }
            
            getData().targetWord.push_back(
                WordUnit(
                    csvWordData[i][getData().rowEnglishWord],
                    csvWordData[i][getData().rowJapaneseMean],
                    tags,
                    weight,
                    next_allowed
                )
            );

            getData().cdf_operated.update(i + 1, weight + (int)((double)weight * getData().adapt_forgetting_curve(i)));
        }
        //難易度係数の合計を計算
        getData().total_weight = getData().cdf_operated.sum(getData().targetWord.size());
        //今回で解いた単語(重複なし)の数を0に。
        getData().uniqueSolvedCount = 0;


        //WordHistory.csvにある前回の日時やその時やった個数を読み込む。

        //やった個数の累計
        getData().totalSolved = ParseOr<long long>(csvWordHistory[0][getData().column1RowTotalDid], 0);

        //前回やった日にちをロード
        //どうやらそのままDateをparseできないみたいなので、自分でyyyy/mm/ddのフォーマットを解析して、Date構造体に突っ込む。
        [&]() {
            String target = ParseOr<String>(csvWordHistory[0][getData().column1RowBeforeDayPlayDate], U"1919/8/10");
            auto separated = splitString(target, U'/');
            getData().beforePlayDate = Date(
                ParseOr<int>(separated[0], 1919),
                ParseOr<int>(separated[1], 8),
                ParseOr<int>(separated[2], 10)
            );
        }();

        //前回の時にその日での累積でやった個数
        getData().beforeSolved = ParseOr<long long>(csvWordHistory[0][getData().column1RowBeforeDayPlayNum], 0);

        //やった個数に関しては、一旦getData().beorePlayDateが同じ日なら累積していき、違う日ならリセットをする。
        if (getData().beforePlayDate.isToday()) {
            getData().todaySolved = getData().beforeSolved;
        }
        else {
            getData().todaySolved = 0;
        }

        //前にやった日ですでにツイート済みか？
        getData().didBeforeTweeted = ParseOr<bool>(csvWordHistory[0][getData().column1RowBeforeDayTweeted], 0);


        //Configuration.csvは以下の関数で別途セットする。
        setConfigurationFromFile(csvConfiguration);

    }

    InitScene(const InitData& init) : IScene(init) {
        readFile();

        welcomeFont = Font(50);

        //今日は前にこれをプレイした日ではない(つまり今日は初めて)の場合、その前の日のデータをツイートする。
        //ただし、getData().didBeforeTweetedがtrueはツイート済みなのでしない。
        //この時、メッセージボックスを出して知らせる
        if (!getData().didBeforeTweeted) {
            
            if (getData().beforePlayDate != Date::Today()) {
                auto res = System::ShowMessageBox(getData().fillNumberToTweet(getData().checkIsTweetAtFirst), MessageBoxButtons::YesNo);
                if (res == MessageBoxSelection::Yes) {
                    Twitter::OpenTweetWindow(getData().fillNumberToTweet(getData().tweetSentence));
                }
            }

            //今日のに関してはまだツイートされてない状態
            getData().didBeforeTweeted = false;
        }
    }

    void update() override {
        changeScene(U"InputAnswerScene", 1);
    }

    void draw() const override {
        welcomeFont(U"Let's improve English!").drawAt(400, 250, Palette::White);
    }
};