package m.alhinc.jp.nakayama_yukiha.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class CalculateSales {
	public static void main(String[] args){

		//マップの生成
		HashMap<String, String> siten = new HashMap<String, String>();
		HashMap<String, String> syohin = new HashMap<String, String>();
//ここから支店定義ファイルの読み込み格納
		//ファイルの読み込み、一行ずつデータを読み、カンマで区切り配列→Mapへ格納
		try{

		File blanch = new File(args[0],"blanch.lst");
		if (!blanch.exists())
		{System.out.println("支店定義ファイルが存在しません");
		return;}


		else{

			FileReader fr = new FileReader(blanch);
		BufferedReader br = new BufferedReader(fr);
		String s;
		while((s = br.readLine()) != null) {
			String[] mise = s.split(",");
			if (!mise[0].matches("^[0-9]{3}$")||mise.length != 2){
				System.out.println("ファイルフォーマットが正しくありません");
				return;
				}
			siten.put(mise[0], mise[1]);
		//エラー表示未着手

		}

		System.out.println(siten.entrySet());
		br.close();

		}
		}catch(IOException e) {
			System.out.println(e);
	}


//ここから商品定義ファイルの読み込み
		try{

			File commodity = new File(args[0],"commodity.lst");
			if (!commodity.exists())
			{System.out.println("商品定義ファイルが存在しません");
			return;
			}
			else{


				FileReader fr = new FileReader(commodity);
			BufferedReader br = new BufferedReader(fr);
			String ss;
			while((ss = br.readLine()) != null) {
				String[] mono = ss.split(",");
				syohin.put(mono[0], mono[1]);

				//エラー表示未着手
			}

			System.out.println(syohin.entrySet());
			br.close();
			}
			} 		catch(IOException e) {
				System.out.println(e);
		}

//ここから売り上げファイルの中身を抽出
		ArrayList<Integer> filename = new ArrayList<Integer>();

File rcdfile = new File(args[0]);
String[] uriage = rcdfile.list();  //ディレクトリ内のファイル名一覧を取得し、string型の配列に返す

 //ただの表示

       for (int ii = 0; ii < uriage.length; ii++){

         if (uriage[ii].matches("\\d{8}.rcd")){   //配列uriageに格納された文字列が8桁.rcdであれば
              String[] struri = uriage[ii].split("\\.");	//ピリオドで分割し
             
//分割した配列をint型に変換
              int n = Integer.parseInt(struri[0]);
              filename.add(n);
         	  }       
             }
          System.out.println(filename);  
//変換したものをチェック
     if (!(filename.get(0) == 1)){
    	 System.out.println("売り上げファイル名が連番になっていません");
    	 return;}
     else{ 
     
    	 	for (int i = 1; i < filename.size(); i++){
				if (!(filename.get(i) == filename.get(i-1)+1)){
					System.out.println("売り上げファイルが連番になっていません");
					return;}
				else{System.out.println("成功です");}
    	 	}			   
              }
	
	
	
	}
      
      }


