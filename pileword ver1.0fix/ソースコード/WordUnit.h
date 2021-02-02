#pragma once

#include<Siv3D.hpp>

class WordUnit {
public:
	//�X�y����ł��̌���@��ʓI�ɂ͉p�P��
	String spellLang;

	//�I�����̕��̌���@��ʓI�ɂ͓��{��̈Ӗ��Ƃ�
	String choosingLang;

	//���̒P��̃^�O
	Array<String> tags;

	//�d�݌W��
	int weight;

	//���ɏo�Ă����͉̂���ڈȍ~���@�Z���Ԃɑ�ʂɏo��̂�h���[�u
	long long next_allowed;


	WordUnit() = default;

	WordUnit(const String _spellLang, const String _choosingLang, Array<String> _tags, int _weight, long long _next_allowed) {
		spellLang = _spellLang, choosingLang = _choosingLang, weight = _weight, tags = _tags, next_allowed = _next_allowed;
	}

	//�ԈႦ����
	int Wrong() {
		const int val = 300;
		weight += val;
		return val;
	}

	//����������
	int Correct() {
		int val = (weight - (int)((double)weight / 1.8));
		weight = (int)((double)weight / 1.8);
		if (weight < 60)weight = 60, val = weight - 60;
		return val;
	}
};