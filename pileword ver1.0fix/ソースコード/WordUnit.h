#pragma once

#include<Siv3D.hpp>

class WordUnit {
public:
	//スペルを打つ方の言語　一般的には英単語
	String spellLang;

	//選択肢の方の言語　一般的には日本語の意味とか
	String choosingLang;

	//この単語のタグ
	Array<String> tags;

	//重み係数
	int weight;

	//次に出ていいのは何語目以降か　短期間に大量に出るのを防ぐ措置
	long long next_allowed;


	WordUnit() = default;

	WordUnit(const String _spellLang, const String _choosingLang, Array<String> _tags, int _weight, long long _next_allowed) {
		spellLang = _spellLang, choosingLang = _choosingLang, weight = _weight, tags = _tags, next_allowed = _next_allowed;
	}

	//間違えた時
	int Wrong() {
		const int val = 300;
		weight += val;
		return val;
	}

	//正解した時
	int Correct() {
		int val = (weight - (int)((double)weight / 1.8));
		weight = (int)((double)weight / 1.8);
		if (weight < 60)weight = 60, val = weight - 60;
		return val;
	}
};