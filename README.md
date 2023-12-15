# KotlinPSI

AndroidでBoringSSLを用いてPSIを行うアプリ

今後の実装

・二者間で通信して共通要素を求める．

・ダミーの接触履歴の共通要素を求める．

2023/12/15

Roomの検索の際にFlow<List<Contact>>型を使えないと思っていたため，
MutableLiveData<LiveData<List<Contact>>>というような構造のデータを使っていたが，
そのせいで12/14にあったような状況になってしまっていたが，Flow<List<Contact>>を使えるように改善した．

参考にしたプロジェクトの
[github]
(https://github.com/google-developer-training/android-basics-kotlin-bus-schedule-app/tree/main)

app/src/main/java/com/example/busschedule/viewmodels/ScheduleListViewModel.kt

2023/12/14

PSIを実行するときに範囲を指定できるようにしている途中

検索して表示する際はLiveData.observeを使用し，recyclerviewに追加していたが，PSIの時はそれが使えない．

observeはデータの変更があった際に実行されるメソッドのため，最初に追加されたデータになってしまう．

データの読み込みが終了したことを示す必要がある．

2023/12/13

外部から読み込んだCSVをデータベースに保存できるようにした．

CSVの中身の時刻表記を`yyyy/MM/dd H:mm`とする必要がある．

3つのボタン
- 読み込んだデータを追加
- 読み込んだデータを入れ替え(データベース内の要素を削除→CSVを追加)
- 一致するデータを削除

2023/12/12

Roomに入力されたデータを検索できるようにした．

4つの検索方法
- すべてのデータを検索する 
- 1ヶ月分のデータを検索する 
- 一致する仮名のデータを検索する 
- 特定の期間のデータを検索する

2023/12/11

サーバの持つ集合を暗号化し，それをクライアントに送信できるようにした．

送信手順

1. サーバーは自分の持っている接触履歴のデータ数(Roomからname要素を取得し，暗号化された数)を送信する．
2. クライアントは受け取ったデータ数をサーバに送る．
3. サーバは受け取った数が正しいものであったら，nameの一つ一つを暗号化したときのサイズ数を送る．
4. クライアントは受け取ったデータ数をサーバに送る．
5. サーバは受け取った数が正しいものであったら，暗号化されたデータを送る．
6. すべてのデータを送るまで繰り返す．

2023/11/28

roomに格納する名前のデータをByteArray型に変更．

PSIを名前の要素のみで実行することにした．

名前のデータをByteArrayにしたため，PSI実行のためにKotlinからC++を呼び出すときに二重ループが必要になった．

(KotlinとC++間でArray<ByteArray>のやり取りのやり方がよくわかっていないため)

2023/11/16

PSIを計算するときにroomから取り出した集合を暗号化できるようにした．

roomから取り出した"yyyy-MM-dd'T'HH:mm"のLocalDateTime型のdate要素をtoString()でStringにしてnativeC++に渡している．

実際にC++側に渡すメッセージはContact(date=2023-11-16T13:06, name=44)のような形になる．

roomの構成を変更．

AsyncTaskを使って操作していたが，lifecyclescopeなどを使用してroomを操作することができるようになった．

非推奨だったAsynkTaskを使わなくなり，Coroutineを使うようになった．

roomの変更に伴い，ContactRepository.ktとContactApplication.ktを追加

2023/11/07

roomのデータベースを操作したときに操作の内容に応じてトーストを表示させるようにした．

2023/11/06

鍵生成，メッセージ暗号化を分けて書くようにした．

鍵生成はおそらく出来ていて，その鍵を使用してメッセージの暗号化も出来ていると思う．

出来ていない部分として暗号化したメッセージをKotlinに持ってくるところである．

jbyteArrayの取り扱いが難しいため，集合の要素数分メッセージ暗号化関数を呼び出す方式になる可能性が高い．

2023/11/01

データベースに格納する日時を日付と時間に分割して文字列として格納する状態から，LocalDateTime型で格納するように変更．

RoomでLocalDateTimeを使用可能にするためにContactConverter.ktファイルを追加．

手動でデータを入力する際はDatePicker,TimePickerを使用している．

プログラム上ではDatePickerのほうが先に記述しているが，TimePickerのほうが先に起動するため，それぞれで`editdatetext.setText()`を行っている．(スレッドなどの問題?)

データベースに格納する際にtry-catchを行うようにした．

データベースに格納できなかった時にToastを表示しようと考えたが，Toastはメインスレッドで動かす必要があったため断念．
(データベース操作に関するフラグを付けてメインスレッド上でToastを実行しようとしたが，複数スレッドでフラグを操作すると同期の問題でうまくいかなかったため放置)

2023/10/27

サーバで暗号化したデータを送信する部分で苦しんでいる．

C++で暗号化したデータをkotlinに持っていくときにjbyteやらjbytearrayなどに直さないと持っていけない可能性がある？

2023/10/17

Roomを用いたデータベースを扱えるようにした．

手動で3つの文字列型データ(`date:String,time:String,name:String`)の追加，
データの削除ができるようになった．

(コルーチンがよくわからなかったためAsyncTask.executeを使用した．)

2023/10/16

通信を行うために`EC_POINT`型のデータをバイナリ形式(`unsigned char *`)に変更可能にした．

バイナリ形式のデータを`EC_POINT`型に変換できるようにした．

2023/10/12

二者間通信を実装する初期段階

2023/10/10

あらかじめ指定した文字列に対して共通要素を求めることができた．
