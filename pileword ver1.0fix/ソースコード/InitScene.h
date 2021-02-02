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
        
        //recentWord���ςɂ��Ƃł���B

    }

    void readFile() {
        //WordData.csv��WordHistory.csv���J���@WordHistory.csv�̍s��������Ȃ��ꍇ��0���߂���B
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
            //BIT��cdf_operated(�ݐϕ��z�֐��̓񕪒T��)�̖�������������B
            std::vector<ll> tmp(csvWordData.rows() + 1, 0);
            getData().cdf_operated = BIT(tmp);
        }

        //Word���Ƃ̈Ӗ����Փx�W���A�^�O��ǂݍ��ޕ���
        int wordNumber = csvWordData.rows();
        for (int i = 0; i < wordNumber; i++) {
            //targetWord��cdf_operated�ɓ����B

            //�V�K�P��̏d�݂�300�Ƃ��Ă���B�����̂�����Ώ㏑������B
            ll weight = 300;
            ll next_allowed = 0;

            //�^�O���^�O����p�P��C���f�b�N�X�̋t����map�ɓo�^���Ă���
            auto tags = splitString(csvWordData[i][getData().rowTag], U' ');
            for (int j = 0; j < tags.size(); j++) {
                getData().tagsToWordsIdx[tags[j]].push_back(i);
            }
            
            //���łɓ�Փx�W�������݂���ꍇ�́A������Q�Ƃ���B
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
        //��Փx�W���̍��v���v�Z
        getData().total_weight = getData().cdf_operated.sum(getData().targetWord.size());
        //����ŉ������P��(�d���Ȃ�)�̐���0�ɁB
        getData().uniqueSolvedCount = 0;


        //WordHistory.csv�ɂ���O��̓����₻�̎����������ǂݍ��ށB

        //��������̗݌v
        getData().totalSolved = ParseOr<long long>(csvWordHistory[0][getData().column1RowTotalDid], 0);

        //�O���������ɂ������[�h
        //�ǂ���炻�̂܂�Date��parse�ł��Ȃ��݂����Ȃ̂ŁA������yyyy/mm/dd�̃t�H�[�}�b�g����͂��āADate�\���̂ɓ˂����ށB
        [&]() {
            String target = ParseOr<String>(csvWordHistory[0][getData().column1RowBeforeDayPlayDate], U"1919/8/10");
            auto separated = splitString(target, U'/');
            getData().beforePlayDate = Date(
                ParseOr<int>(separated[0], 1919),
                ParseOr<int>(separated[1], 8),
                ParseOr<int>(separated[2], 10)
            );
        }();

        //�O��̎��ɂ��̓��ł̗ݐςł������
        getData().beforeSolved = ParseOr<long long>(csvWordHistory[0][getData().column1RowBeforeDayPlayNum], 0);

        //��������Ɋւ��ẮA��UgetData().beorePlayDate���������Ȃ�ݐς��Ă����A�Ⴄ���Ȃ烊�Z�b�g������B
        if (getData().beforePlayDate.isToday()) {
            getData().todaySolved = getData().beforeSolved;
        }
        else {
            getData().todaySolved = 0;
        }

        //�O�ɂ�������ł��łɃc�C�[�g�ς݂��H
        getData().didBeforeTweeted = ParseOr<bool>(csvWordHistory[0][getData().column1RowBeforeDayTweeted], 0);


        //Configuration.csv�͈ȉ��̊֐��ŕʓr�Z�b�g����B
        setConfigurationFromFile(csvConfiguration);

    }

    InitScene(const InitData& init) : IScene(init) {
        readFile();

        welcomeFont = Font(50);

        //�����͑O�ɂ�����v���C�������ł͂Ȃ�(�܂荡���͏��߂�)�̏ꍇ�A���̑O�̓��̃f�[�^���c�C�[�g����B
        //�������AgetData().didBeforeTweeted��true�̓c�C�[�g�ς݂Ȃ̂ł��Ȃ��B
        //���̎��A���b�Z�[�W�{�b�N�X���o���Ēm�点��
        if (!getData().didBeforeTweeted) {
            
            if (getData().beforePlayDate != Date::Today()) {
                auto res = System::ShowMessageBox(getData().fillNumberToTweet(getData().checkIsTweetAtFirst), MessageBoxButtons::YesNo);
                if (res == MessageBoxSelection::Yes) {
                    Twitter::OpenTweetWindow(getData().fillNumberToTweet(getData().tweetSentence));
                }
            }

            //�����̂Ɋւ��Ă͂܂��c�C�[�g����ĂȂ����
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