#pragma once

#include <Siv3D.hpp>
#include <any>
#include <algorithm>

#include "WordUnit.h"

typedef long long ll;

//BIT��1-idx
struct BIT {
	std::vector<ll> bit;
	int N;

	BIT(std::vector<ll> data) {
		bit = data;
		N = bit.size() - 1;
	}

	BIT() = default;

	//�����X�V����I
	void update(int pos, ll x) {
		for (int i = pos; i <= N; i += (i & (-i)))
			bit[i] += x;
	}

	ll sum(int a) {
		//[1, a]�̘a�����߂�
		ll ret = 0;
		for (int i = a; i > 0; i -= (i & (-i)))
			ret += bit[i];
		return ret;
	}
	ll sum(int a, int b) {
		//[a, b]�̘a�����߂�
		return sum(b) - sum(a - 1);
	}
	ll lower_bound(ll w) {
		if (w <= 0)return 0;
		int a = 1;
		while (a < N)a <<= 1;
		a /= 2;
		if (a * 2 == N)a <<= 1;
		int x = 0;
		for (int k = a; k > 0; k >>= 1) {
			if (x + k <= N && w > bit[x + k]) {
				w -= bit[x + k];
				x += k;
			}
		}
		return x + 1;
	}
};

//��������w��̂��̂�Vector��Split����B
Array<String> splitString(const String& target, const char32& split_char) {
	Array<String> ret;
	String buf = U"";
	for (int i = 0; i < target.size(); i++) {
		if (target[i] == split_char) {
			if (!buf.empty())
				ret.push_back(buf);
			buf.clear();
		}
		else {
			buf += target[i];
		}
	}
	if(!buf.empty())
		ret.push_back(buf);

	return ret;
}

//���ʃf�[�^

struct CommonData {

	Array<WordUnit> targetWord;

	//���I�����Ă���P���index�B
	int nowTestedIdx;

	//TODO ������ւ��struct ConfigParams�̒��Ɋ܂܂�Ă���̂̂悤�ɂ��Č�������B
	//Configuration.csv�֘A�̃p�����[�^���i�[����map
	std::map<String, std::any> ConfigParams;
	
	//Configuration.csv�̃p�����[�^�̃��x���̕�����
	const String rateSpellToMeanChoosing = U"rateSpellToMeanChoosing";
	const String rateMeanToSpellTyping = U"rateMeanToSpellTyping";
	const String rateMeanToSpellChoosing = U"rateMeanToSpellChoosing";
	const String doShowSynonym = U"doShowSynonym";

	//WordData.csv�̓ǂݍ��݊֘A
	//1��ڂ͉p�P��
	const int rowEnglishWord = 0;
	//2��ڂ̓^�O ���p�X�y�[�X�ŋ敪����
	const int rowTag = 1;
	//3��ڂ͓��{�����
	const int rowJapaneseMean = 2;
	//�^�O�Ɋ܂܂��P���index�B
	std::map<String, Array<int>> tagsToWordsIdx;

	//WordHistory.csv�̓ǂݍ��݊֘A
	const int rowDifficulty = 0;
	const int rowBeforeOccurency = 1;
	const int column1RowTotalDid = 0;
	const int column1RowBeforeDayPlayDate = 1;
	const int column1RowBeforeDayPlayNum = 2;
	const int column1RowBeforeDayTweeted = 3;

	BIT cdf_operated;
	ll total_weight = 0;

	long long memoedTimeMili;

	bool isCorrect;

	//0�Ȃ����->spell�Ń^�C�s���O�@1�Ȃ����->spell�Ŏl���@2�Ȃ�X�y��->�����̎l�� -1�Ȃ�R�}���h���[�h
	int problemMode = 0;

	//�݌v�� ���ꂩ��t�@�C������ǂݍ���
	long long totalSolved = 0;
	//�{���̉񐔁@�����ς���ĂȂ����beforeSolved����ݐς��Ă����B
	long long todaySolved = 0;
	//�O�ɂ�������̉񐔁@�t�@�C������ǂݍ���
	long long beforeSolved = 0;
	//�O�ɂ�������@�t�@�C������ǂݍ���
	Date beforePlayDate;
	//�O�ɂ�������̓����łɃc�C�[�g���ꂽ���@�t�@�C������ǂݍ���
	bool didBeforeTweeted = false;

	//����̉񐔁@�����0���琔���Ă����B
	long long thisTrySolved = 0;

	std::map<String, bool> isSolved;
	int uniqueSolvedCount = 0;

	double adapt_forgetting_curve(int x) {
		int total = targetWord.size();
		if (x >= total - 50)return (double)total * 0.006;
		return 0;
	}

	//�c�C�[�g����镶��
	//%d�ł͓K�؂Ȑ���������B
	//TODO �V�K�p�P��̃J�E���g���ł���悤��
	const String tweetSentence = U"%d/%d/%d\n�p�P�ꕜ�K%d��\n�����܂����I\n\n#pileword #�p�P��\n";
	//�O�ɂ�������̐��i���c�C�[�g���Ă��Ȃ��Ƃ��̃��b�Z�[�W�{�b�N�X�̕���
	const String checkIsTweetAtFirst = U"%d/%d/%d�̊w�K�̓c�C�[�g����Ă���܂��񂪁A���܂����H\n(�p�P��%d��)";

	//���2�̃��b�Z�[�W����%d��K�؂Ȓl�Ŗ��߂�֐��B
	//���ʂƂ���Tweet�p��������Ȃ��@TODO �����Ǝ��Ԃ��ł����番������ׂ�
	String fillNumberToTweet(const String& target) {
		String ret = U"";
		int cnt = 0;
		for (int i = 0; i < target.size(); i++) {
			if (i + 1 < target.size() && target[i] == U'%' && target[i + 1] == U'd') {
				switch (cnt) {
				case 0:
					//�N
					ret += Format(beforePlayDate.year);
					break;
				case 1:
					//��
					ret += Format(beforePlayDate.month);
					break;
				case 2:
					//��
					ret += Format(beforePlayDate.day);
					break;
				case 3:
					//�����������
					//�������ꂪ�����I�ɂ��ꂽ�c�C�[�g(�O���̊w�K�̕񍐂��ׂ���Ă��Ȃ�)���́A�O���̂��Q�Ƃ��Ȃ��Ƃ����Ȃ��B
					//�Ƃ����킯�œǂݍ��񂾓��ɂ��ƍ������Ⴄ�̂Ȃ�΁A�O���̊w�K�L�^���A�����łȂ���΍����̊w�K�L�^�𖄂߂�B
					ret += Format(
						beforePlayDate.isToday() ? todaySolved : beforeSolved
					);
					break;
				}
				cnt++;
				i++;
			}
			else {
				ret += target[i];
			}
		}
		return ret;
	}

};

using App = SceneManager<String, CommonData>;
