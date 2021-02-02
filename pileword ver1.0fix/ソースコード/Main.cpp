
#include <Siv3D.hpp>

#include "InitScene.h"
#include "InputAnswerScene.h"
#include "ShowWordDetailScene.h"

/*
TODO
終了時のファイル保存処理
Spell->choosingの時にのみ表記するワード、chossing->Spellの時にのみ表記するワードを適用

*/

void writeFile(App& _m) {
    TextWriter csvHistoryData(U"WordHistory.csv");
    if (!csvHistoryData) {
        throw Error(U"WordHistory.csv doesn't exist or is still opened so siv3D cannot write.");
    }

    auto target = _m.get()->targetWord;

    //1行目に書き込むもの
    csvHistoryData.write(_m.get()->totalSolved);//合計で解いた数
    csvHistoryData.write(U",");
    csvHistoryData.write(Date::Today());
    csvHistoryData.write(U",");
    csvHistoryData.write(_m.get()->todaySolved);
    csvHistoryData.write(U",");
    csvHistoryData.write(_m.get()->didBeforeTweeted);
    csvHistoryData.write(U"\n");

    //各単語ごとの難易度係数を書き込む
    for (int i = 0; i < target.size(); i++) {
        csvHistoryData.write(target[i].weight);
        csvHistoryData.write(U",");
        csvHistoryData.write(target[i].next_allowed);
        csvHistoryData.write(U"\n");
    }

}

void Main()
{

    App manager;
    manager.add<InitScene>(U"InitScene");
    manager.add<InputAnswerScene>(U"InputAnswerScene");
    manager.add<ShowWordDetailScene>(U"ShowWordDetailScene");

    while (System::Update())
    {
        if (!manager.update()) {
            break;
        }
    }

    writeFile(manager);

}

//
// = アドバイス =
// Debug ビルドではプログラムの最適化がオフになります。
// 実行速度が遅いと感じた場合は Release ビルドを試しましょう。
// アプリをリリースするときにも、Release ビルドにするのを忘れないように！
//
// 思ったように動作しない場合は「デバッグの開始」でプログラムを実行すると、
// 出力ウィンドウに詳細なログが表示されるので、エラーの原因を見つけやすくなります。
//
// = お役立ちリンク =
//
// OpenSiv3D リファレンス
// https://siv3d.github.io/ja-jp/
//
// チュートリアル
// https://siv3d.github.io/ja-jp/tutorial/basic/
//
// よくある間違い
// https://siv3d.github.io/ja-jp/articles/mistakes/
//
// サポートについて
// https://siv3d.github.io/ja-jp/support/support/
//
// Siv3D ユーザコミュニティ Slack への参加
// https://siv3d.github.io/ja-jp/community/community/
//
// 新機能の提案やバグの報告
// https://github.com/Siv3D/OpenSiv3D/issues
//
