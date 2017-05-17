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
	public static void main(String[] args){

		//マップの生成
		HashMap<String, String> branchcode = new HashMap<String, String>();
		HashMap<String, String> commoditycode = new HashMap<String, String>();
		HashMap<String, Long> branchsales = new HashMap<String, Long>();
		HashMap<String, Long> commoditysales = new HashMap<String, Long>();
		//コマンドライン引数の要素数確認
		if (args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//ここから支店定義ファイルの読み込み格納
		//ファイルの読み込み、一行ずつデータを読み、カンマで区切り配列→Mapへ格納

		BufferedReader br = null;
		try{

			File branch = new File(args[0],"branch.lst");
			if (!branch.exists()) {
				System.out.println("支店定義ファイルが存在しません");
				return;
			}

			FileReader fr = new FileReader(branch);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] mise = s.split(",");

				//支店名は○○支店の表記、それ以外は処理終了

				if (!mise[0].matches("^[0-9]{3}$")||mise.length !=2 ){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}

				branchcode.put(mise[0], mise[1]);
				branchsales.put(mise[0], 0L);
			}

		}catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				if (br != null){
				br.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}
		}

//ここから商品定義ファイルの読み込み

		BufferedReader brr = null;
		try{

			File commodity = new File(args[0],"commodity.lst");
			if (!commodity.exists()){
				System.out.println("商品定義ファイルが存在しません");
				return;
			}

			FileReader fr = new FileReader(commodity);
			brr = new BufferedReader(fr);
			String ss;
			while((ss = brr.readLine()) != null) {
				String[] mono = ss.split(",");
				if (!mono[0].matches("^[A-Za-z0-9]{8}$")||mono.length != 2){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commoditycode.put(mono[0], mono[1]);
				commoditysales.put(mono[0], 0L);
			}
		}catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
		}finally{
			try{
				if (brr != null){
				brr.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}
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
					System.out.println(earningfiles[ii].getName() + "のフォーマットが不正です");
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
			System.out.println("予期せぬエラーが発生しました");

		}finally{
			try{
				if (bbrr != null){
				bbrr.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}
		}



		//＊＊＊集計に成功した場合にファイルを作成するif文を後で作成＊＊＊＊＊＊＊

		//＊＊Mapをソートするプログラム*＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊

		List<Map.Entry<String,Long>> entries =
				new ArrayList<Map.Entry<String, Long>>(branchsales.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>(){
			@Override
			public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		BufferedWriter bbww = null;
		try{
			File brancho = new File(args[0],"branch.out");
			FileWriter ffww = new FileWriter(brancho);
			bbww = new BufferedWriter(ffww);

			for (Entry<String,Long> s : entries) {


				String val = String.valueOf(s.getValue());

				//ファイルへ出力
				bbww.write(s.getKey() + "," + branchcode.get(s.getKey()) + "," + val);
				bbww.newLine();

			}bbww.close();
		}catch (FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しました");
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
		}finally{
			try{
				if (bbww != null){
				bbww.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}
		}

		//ここから商品売り上げまとめ
		//Mapをソートする***********＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊
		List<Map.Entry<String,Long>> entriess =
				new ArrayList<Map.Entry<String, Long>>(commoditysales.entrySet());
		Collections.sort(entriess, new Comparator<Map.Entry<String,Long>>(){
				@Override
				public int compare(
						Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		//ファイルに書き込みする*＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊
		BufferedWriter bbwww = null;
		try{
			File commodityo = new File(args[0],"commodity.out");
			FileWriter ffwww = new FileWriter(commodityo);
			bbwww = new BufferedWriter(ffwww);

			for (Entry<String,Long> ss : entriess) {

				String val2 = String.valueOf(ss.getValue());

				//ファイルへ出力
				bbwww.write(ss.getKey() + "," + commoditycode.get(ss.getKey()) + "," + val2);
				bbwww.newLine();

			}bbwww.close();
		}catch (FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しました");
		}catch (IOException e){
			System.out.println("予期せぬエラーが発生しました");
		}finally{
			try{
				if (bbwww != null){
				bbwww.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			}
		}


	}

}



