#pragma once

#include <Siv3D.hpp>

#include "basement.h"

class ShowWordDetailScene : public App::Scene {

private:

    Font showingMiddleWordFont, showingWordFont, showingLargeWordFont;

    //ì¸óÕÇ≥ÇÍÇΩï∂éöóÒÇÃString
    String inputText;

    String showingWord;

    //à”ñ°Ç™í∑Ç¢Ç∆Ç´Ç…ê‹ÇËï‘Ç≥ÇπÇÈdrawÇµÇ»Ç¢ÉtÉåÅ[ÉÄ
    Rect choosingLangFrame;
    
    bool isCorrect;

public:
    ShowWordDetailScene(const InitData& init) : IScene(init) {
        /*
        showingMiddleWordFont = Font(25);
        showingWordFont = Font(20);
        showingLargeWordFont = Font(50);
        */
        
        //U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf"
        showingMiddleWordFont = Font(25, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
        showingWordFont = Font(20, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
        showingLargeWordFont = Font(50, U"example/font/NotoSansCJKjp/NotoSansCJKjp-Regular.otf");
        
        choosingLangFrame = Rect(100, 300, 600, 200);
    }

    void update() override {
        if ((KeyEnter | KeySpace).up()) {
            changeScene(U"InputAnswerScene", 0.2);
        }
    }

    void draw() const override {
        /*
        showingWordFont(U"Total Solved: " + Format(getData().totalSolved)).draw(500, 50, Palette::Black);
        showingWordFont(U"This Try Solved: " + Format(getData().thisTrySolved)).draw(500, 100, Palette::Black);
        showingWordFont(U"This Try Solved Uniquely: " + Format(getData().uniqueSolvedCount)).draw(450, 150, Palette::Black);
        */


        showingLargeWordFont(U"Wrong!").drawAt(150, 100, Palette::Red);

        showingMiddleWordFont(getData().targetWord[getData().nowTestedIdx].spellLang).drawAt(300, 250, Palette::Black);
        showingMiddleWordFont(getData().targetWord[getData().nowTestedIdx].choosingLang).draw(choosingLangFrame, Palette::Black);
    }

};