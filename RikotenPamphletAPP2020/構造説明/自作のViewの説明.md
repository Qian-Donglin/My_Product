* com.rikoten.AndroidApp2018new.SharingMaterial.AutoMeasuredListView

もともと、ListViewはScrollViewの中に入れたまま、ListViewの中身をAdapterで設定すると、ScrollView内に含まれているListView自体の表示がうまくいかないということがある。
これは、**ScrollViewは、子View(Group)の高さが明確でなければならない**というルールがあります。なので、最初のListViewの中身でScrollViewが決められて、その後ListViewにAdapterを介して中身をリセットしてもScroolViewはそれに対応できない(ここの説明怪しい　時間あったら訂正したいね)。
これに対応するために、以上のようなListViewを拡張したものを作った。使用方法はListViewと全く同じである。

参考文献：
https://qiita.com/naodroid/items/d685a0113342edbb7587
onMeasure()でのサイズ決定について日本語でやさしく解説してるサイト。リファレンスを見ながらこれを見ると理解が進みやすい。
https://blog.howtelevision.co.jp/entry/2015/03/19/115020
参考というかほとんど実質的にパクってしまった

**ちなみに、ListViewでは以上の問題が起きるが、RecyclerViewはどうやら起きないらしい**(ソースen：https://stackoverrun.com/ja/q/12056493)
