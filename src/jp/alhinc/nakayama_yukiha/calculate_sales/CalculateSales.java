package jp.alhinc.nakayama_yukiha.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {
	//定義ファイルの読み込みメソッド
	//dir＝ディレクトリをあらわす引数
	//file＝呼び出すファイル名
	//name＝定義ファイルの日本語名
	//match＝ファイルの中身を判別する条件
	static boolean fileimp(String dir, String file, String name, String match, 
			HashMap<String,String> code, HashMap<String,Long>sales){
		BufferedReader br = null;
		try{

			File impfile = new File(dir,file);
			if (!impfile.exists()) {
				System.out.println(name + "定義ファイルが存在しません");
				return false;
			}

			FileReader fr = new FileReader(impfile);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] arra = s.split(",");

				//支店名は○○支店の表記、それ以外は処理終了

				if (!arra[0].matches(match)||arra.length !=2 ){
					System.out.println(name + "定義ファイルのフォーマットが不正です");
					return false;
				}

				code.put(arra[0], arra[1]);
				sales.put(arra[0], 0L);
			}

		}catch(IOException e) {
			System.out.println("予期せぬエラーが発生しましたa");
			return false;
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("予期せぬエラーが発生しましたb");
			return false;
		}finally{
			try{
				if (br != null){
				br.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しましたc");
				return false;
			}
		}return true;
	}



	//ファイルへ書き込みメソッド
	static boolean fileout(String dir, String file, HashMap<String,String> code, HashMap<String,Long>sales){
		List<Map.Entry<String,Long>> entries =
				new ArrayList<Map.Entry<String, Long>>(sales.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>(){
			@Override
			public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		BufferedWriter bbww = null;
		try{
			File outfile = new File(dir,file);
			FileWriter ffww = new FileWriter(outfile);
			bbww = new BufferedWriter(ffww);

			for (Entry<String,Long> s : entries) {


				String val = String.valueOf(s.getValue());

				//ファイルへ出力
				bbww.write(s.getKey() + "," + code.get(s.getKey()) + "," + val);
				bbww.newLine();

			}
		}catch (FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しましたd");
			return false;
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しましたe");
			return false;
		}finally{
			try{
				if (bbww != null){
					bbww.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しましたf");
				return false;
			}
		}
		return true;
	}
	
	//実行メソッド開始
	public static void main(String[] args){

		//マップの生成
		HashMap<String, String> branchcode = new HashMap<String, String>();
		HashMap<String, String> commoditycode = new HashMap<String, String>();
		HashMap<String, Long> branchsales = new HashMap<String, Long>();
		HashMap<String, Long> commoditysales = new HashMap<String, Long>();
		//コマンドライン引数の要素数確認
		if (args.length != 1){
			System.out.println("予期せぬエラーが発生しましたa");
			return;
		}

		
		//支店定義ファイルの読み込み格納

		if (!fileimp(args[0], "branch.lst", "支店", "^[0-9]{3}$", branchcode, branchsales)){
			return;
		}

		//商品定義ファイルの読み込み

		if (!fileimp(args[0], "commodity.lst", "商品", "^[A-Za-z0-9]{8}$", commoditycode, commoditysales)){
			return;
		}
				
				
		//ここから売り上げファイルの中身を抽出
		ArrayList<Integer> filename = new ArrayList<Integer>();
		//ファイル名を格納するリストを作成

		File rcdfile = new File(args[0]);
		String[] earning = rcdfile.list();
		//ディレクトリ内のファイル名一覧を取得し、string型の配列に返す
		//listfilesを使用するとファイルとして一覧を取得することができる。getName()を使用するとファイル名を抽出
		//ただの表示

		for (int ii = 0; ii < earning.length; ii++){

			if (earning[ii].matches("\\d{8}.rcd")){
				//配列earningに格納された文字列が数字8桁.rcdであれば
				String[] struri = earning[ii].split("\\.");	//ピリオドで分割し

				//分割した配列をint型に変換
				int n = Integer.parseInt(struri[0]);
				filename.add(n);
				Collections.sort(filename);
			}
		}

		//連番チェック
		for (int i = 1; i < filename.size(); i++){
			if (!(filename.get(i) == filename.get(i-1)+1)){
				System.out.println("売上ファイル名が連番になっていません");
				return;}
		}

		//フィルタを作成

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name){
				if(name.matches("\\d{8}.rcd")){
					return true;
				}else{
					return false;
				}
			}
		};
		//！ファイルの読み込み開始！

		BufferedReader bbrr = null;
		try{
			File dir = new File(args[0]);
			File[] earningfiles = dir.listFiles(filter);

			for (int i = 0 ; i < earningfiles.length ; i++){
				if (earningfiles[i].isDirectory()){
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}

			//！フィルターをかけたリストをファイルの数だけ読み込み！
			for (int ii = 0; ii < earningfiles.length; ii++){
				ArrayList<String> earningf = new ArrayList<String>();
				FileReader frr = new FileReader(earningfiles[ii]);
				bbrr = new BufferedReader(frr);

				//！読み込んだファイルを一行ずつ読み込みリストに格納！
				String rl;
				for (int iii = 0; (rl = bbrr.readLine()) != null; iii++){
					earningf.add(rl);

				}

				int size = earningf.size();
				if (!String.valueOf(size).matches("3")){
					System.out.println(earningfiles[ii].getName() + "のフォーマットが不正です");
					return;
				}
				if (!earningf.get(2).matches("^[0-9]+$")){
					System.out.println("予期せぬエラーが発生しましたg");//修正
					return;
				}


				//！リストからMapへ集計
				//MapにKeyが格納されているか確認
				if (branchsales.containsKey(earningf.get(0))){
					long S1 = (branchsales.get(earningf.get(0)));
					long S2 = Long.parseLong(earningf.get(2));
					long S3 = (S1 + S2);

					if (String.valueOf(S3).matches("^[0-9]{11}$")){
						System.out.println("合計金額が10桁を超えました");
						return;
					}
					branchsales.put(earningf.get(0), S3);
				}else{
					System.out.println(earningfiles[ii].getName() + "の支店コードが不正です");
					return;
				}
				//MapにKeyが格納されているか確認
				if (commoditysales.containsKey(earningf.get(1))){
					long l1 = (commoditysales.get(earningf.get(1)));
					long l2 = Long.parseLong(earningf.get(2));
					long l3 = (l1 + l2);

					if (String.valueOf(l3).matches("^[0-9]{11}$")){
						System.out.println("合計金額が10桁を超えました");
						return;
					}
					commoditysales.put(earningf.get(1), l3);

				}else{
					System.out.println(earningfiles[ii].getName() + "の商品コードが不正です");
					return;
				}

			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しましたh");
			return;

		}finally{
			try{
				if (bbrr != null){
				bbrr.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しましたi");
			}
		}

		//＊＊Mapをソートしてファイルへ書き込むメソッドの呼び出し*＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊

		//支店別
		if (!fileout(args[0], "branch.out", branchcode, branchsales)){
			return;
		}

		//商品別
		if (!fileout(args[0], "commodity.out", commoditycode, commoditysales)){
			return;
		}

			
	}

}



