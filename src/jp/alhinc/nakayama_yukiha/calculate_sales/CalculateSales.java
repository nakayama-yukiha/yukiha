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
	//***************************************************
	//dir＝ディレクトリをあらわす引数
	//file＝呼び出すファイル名
	//name＝定義ファイルの日本語名
	//match＝ファイルの中身を判別する条件

	static boolean fileImport(String dir, String file, String name, String match,
			HashMap<String,String> code, HashMap<String,Long>sales){

		BufferedReader br = null;
		try{

			File impFile = new File(dir,file);
			if (!impFile.exists()) {
				System.out.println(name + "定義ファイルが存在しません");
				return false;
			}

			FileReader fr = new FileReader(impFile);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] codeName = s.split(",");

				//支店名は○○支店の表記、それ以外は処理終了

				if (codeName.length !=2 || !codeName[0].matches(match)){
					System.out.println(name + "定義ファイルのフォーマットが不正です");
					return false;
				}

				code.put(codeName[0], codeName[1]);
				sales.put(codeName[0], 0L);
			}

		}catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if (br != null){
					br.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}return true;
	}

	//ファイルへ書き込みメソッド
	//************************************************
	//dir＝ディレクトリ
	//file＝ファイル名
	//code＝商品定義マップ名
	//sales＝金額マップ名

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

		BufferedWriter bw = null;
		try{
			File outFile = new File(dir,file);
			FileWriter fw = new FileWriter(outFile);
			bw = new BufferedWriter(fw);

			for (Entry<String, Long> s : entries) {

				//ファイルへ出力
				bw.write(s.getKey() + "," + code.get(s.getKey()) + "," + s.getValue());
				bw.newLine();

			}

		}catch (FileNotFoundException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			try{
				if (bw != null){
					bw.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}

	//実行メソッド開始
	public static void main(String[] args){

		//マップの生成
		HashMap<String, String> branchCode = new HashMap<String, String>();
		HashMap<String, String> commodityCode = new HashMap<String, String>();
		HashMap<String, Long> branchSales = new HashMap<String, Long>();
		HashMap<String, Long> commoditySales = new HashMap<String, Long>();
		//コマンドライン引数の要素数確認
		if (args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//支店定義ファイルの読み込み格納
		if (!fileImport(args[0], "branch.lst", "支店", "^[0-9]{3}$", branchCode, branchSales)){
			return;
		}

		//商品定義ファイルの読み込み
		if (!fileImport(args[0], "commodity.lst", "商品", "^[A-Za-z0-9]{8}$", commodityCode, commoditySales)){
			return;
		}

		//ここから売り上げファイルの中身を抽出
		ArrayList<String> fileName = new ArrayList<String>();
		//ファイル名を格納するリストを作成

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name){
				File file = new File(dir, name);
				if(file.isFile() && name.matches("\\d{8}.rcd")){
					return true;
				}else{
					return false;
				}
			}
		};

		File rcdFile = new File(args[0]);
		File[] earning = rcdFile.listFiles(filter);

		for (int i = 0; i < earning.length; i++){
			fileName.add(earning[i].getName());
		}
		Collections.sort(fileName);

		for (int i = 1; i < fileName.size(); i++){
			String fileName2 = fileName.get(i).substring(1, 8);
			int fileN = Integer.parseInt(fileName2);
			String fileName3 = fileName.get(i-1).substring(1, 8);
			int fileN2 = Integer.parseInt(fileName3);

			if (!(fileN == fileN2+1)){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		BufferedReader br = null;
		try{
			for (int i = 0; i < fileName.size(); i++){
				ArrayList<String> earningF = new ArrayList<String>();
				File dir = new File(args[0],fileName.get(i));
				FileReader fr = new FileReader(dir);
				br = new BufferedReader(fr);

				//！読み込んだファイルを一行ずつ読み込みリストに格納！
				String rl;
				for (int j = 0; (rl = br.readLine()) != null; j++){
					earningF.add(rl);
				}

				int size = earningF.size();
				if (!(size == 3)){
					System.out.println(fileName.get(i) + "のフォーマットが不正です");
					return;
				}
				if (!earningF.get(2).matches("^[0-9]+$")){
					System.out.println("予期せぬエラーが発生しました");//修正
					return;
				}

				//！リストからMapへ集計
				//MapにKeyが格納されているか確認
				if (!branchSales.containsKey(earningF.get(0))){
					System.out.println(fileName.get(i) + "の支店コードが不正です");
					return;
				}
				//MapにKeyが格納されているか確認
				if (!commoditySales.containsKey(earningF.get(1))){
					System.out.println(fileName.get(i) + "の商品コードが不正です");
					return;
				}

				long S2 = Long.parseLong(earningF.get(2));

				long S1 = (branchSales.get(earningF.get(0)));
				long S3 = (S1 + S2);

				if (String.valueOf(S3).matches("^[0-9]{11}$")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				long l1 = (commoditySales.get(earningF.get(1)));
				long l3 = (l1 + S2);

				if (String.valueOf(l3).matches("^[0-9]{11}$")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchSales.put(earningF.get(0), S3);
				commoditySales.put(earningF.get(1), l3);

			}
		}catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			try{
				if (br != null){
					br.close();
				}
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		//＊＊Mapをソートしてファイルへ書き込むメソッドの呼び出し*＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊
		//支店別
		if (!fileout(args[0], "branch.out", branchCode, branchSales)){
			return;
		}

		//商品別
		if (!fileout(args[0], "commodity.out", commodityCode, commoditySales)){
			return;
		}

	}

}



