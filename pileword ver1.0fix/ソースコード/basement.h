#pragma once

#include <Siv3D.hpp>
#include <any>
#include <algorithm>

#include "WordUnit.h"

typedef long long ll;

//BITは1-idx
struct BIT {
	std::vector<ll> bit;
	int N;

	BIT(std::vector<ll> data) {
		bit = data;
		N = bit.size() - 1;
	}

	BIT() = default;

	//差分更新せよ！
	void update(int pos, ll x) {
		for (int i = pos; i <= N; i += (i & (-i)))
			bit[i] += x;
	}

	ll sum(int a) {
		//[1, a]の和を求める
		ll ret = 0;
		for (int i = a; i > 0; i -= (i & (-i)))
			ret += bit[i];
		return ret;
	}
	ll sum(int a, int b) {
		//[a, b]の和を求める
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

//文字列を指定のものでVectorにSplitする。
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

//共通データ

struct CommonData {

	Array<WordUnit> targetWord;

	//今選択している単語のindex。
	int nowTestedIdx;

	//TODO ここらへんをstruct ConfigParamsの中に含まれてるもののようにして後程治す。
	//Configuration.csv関連のパラメータを格納するmap
	std::map<String, std::any> ConfigParams;
	
	//Configuration.csvのパラメータのラベルの文字列
	const String rateSpellToMeanChoosing = U"rateSpellToMeanChoosing";
	const String rateMeanToSpellTyping = U"rateMeanToSpellTyping";
	const String rateMeanToSpellChoosing = U"rateMeanToSpellChoosing";
	const String doShowSynonym = U"doShowSynonym";

	//WordData.csvの読み込み関連
	//1列目は英単語
	const int rowEnglishWord = 0;
	//2列目はタグ 半角スペースで区分する
	const int rowTag = 1;
	//3列目は日本語説明
	const int rowJapaneseMean = 2;
	//タグに含まれる単語のindex。
	std::map<String, Array<int>> tagsToWordsIdx;

	//WordHistory.csvの読み込み関連
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

	//0なら説明->spellでタイピング　1なら説明->spellで四択　2ならスペル->説明の四択 -1ならコマンドモード
	int problemMode = 0;

	//累計回数 これからファイルから読み込む
	long long totalSolved = 0;
	//本日の回数　日が変わってなければbeforeSolvedから累積していく。
	long long todaySolved = 0;
	//前にやった日の回数　ファイルから読み込む
	long long beforeSolved = 0;
	//前にやった日　ファイルから読み込む
	Date beforePlayDate;
	//前にやったその日すでにツイートされたか　ファイルから読み込む
	bool didBeforeTweeted = false;

	//今回の回数　これは0から数えていく。
	long long thisTrySolved = 0;

	std::map<String, bool> isSolved;
	int uniqueSolvedCount = 0;

	double adapt_forgetting_curve(int x) {
		int total = targetWord.size();
		if (x >= total - 50)return (double)total * 0.006;
		return 0;
	}

	//ツイートされる文章
	//%dでは適切な数字が入る。
	//TODO 新規英単語のカウントもできるように
	const String tweetSentence = U"%d/%d/%d\n英単語復習%d語\nをやりました！\n\n#pileword #英単語\n";
	//前にやった日の精進をツイートしていないときのメッセージボックスの文章
	const String checkIsTweetAtFirst = U"%d/%d/%dの学習はツイートされておりませんが、しますか？\n(英単語%d個)";

	//上の2つのメッセージ内の%dを適切な値で埋める関数。
	//結果としてTweet用だけじゃない　TODO ちゃんと時間ができたら分離するべき
	String fillNumberToTweet(const String& target) {
		String ret = U"";
		int cnt = 0;
		for (int i = 0; i < target.size(); i++) {
			if (i + 1 < target.size() && target[i] == U'%' && target[i + 1] == U'd') {
				switch (cnt) {
				case 0:
					//年
					ret += Format(beforePlayDate.year);
					break;
				case 1:
					//月
					ret += Format(beforePlayDate.month);
					break;
				case 2:
					//日
					ret += Format(beforePlayDate.day);
					break;
				case 3:
					//今日やった数
					//もしこれが自動的にされたツイート(前日の学習の報告が為されていない)時は、前日のを参照しないといけない。
					//というわけで読み込んだ日にちと今日が違うのならば、前日の学習記録を、そうでなければ今日の学習記録を埋める。
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
