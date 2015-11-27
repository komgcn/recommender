import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ItemCF {
	final String connection_string = "jdbc:sqlite:comp3208-2015.db";
	public static Connection c;
	static ArrayList<int[]> ratings, test;

	public ItemCF() {
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(connection_string);
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");
		} catch (Exception e) {
			error(e);
		}
	}

	public void loadRatings() {
		try {
			System.out.println("Loading ratings");
			Statement stat = c.createStatement();
			ResultSet rs = stat.executeQuery("select * from traindata");
			ratings = new ArrayList<>();
			while (rs.next()) {
				int[] rating = new int[3];
				rating[0] = rs.getInt(1);
				rating[1] = rs.getInt(2);
				rating[2] = rs.getInt(3);
				ratings.add(rating);
			}
			System.out.println("Done");
		} catch (Exception e) {
			error(e);
		}
	}

	public void loadTest() {
		try {
			System.out.println("Loading test");
			Statement stat = c.createStatement();
			ResultSet rs = stat.executeQuery("select * from testdata");
			test = new ArrayList<>();
			while (rs.next()) {
				int[] rating = new int[2];
				rating[0] = rs.getInt(1);
				rating[1] = rs.getInt(2);
				test.add(rating);
			}
			System.out.println("Done");
		} catch (Exception e) {
			error(e);
		}
	}

	public void error(Exception e) {
		System.err.println(e.getClass().getName() + ": " + e.getMessage());
		System.exit(0);
	}

	public static void main(String[] args) {
		ItemCF cf = new ItemCF();
		cf.loadRatings();
		cf.loadTest();

		TreeMap<Integer, HashMap<Integer,Integer>> userRate = new TreeMap<>();
		System.out.println("Loading user rating");
		for (int[] rate : ratings) {
			if (userRate.containsKey(rate[0])) {
				userRate.get(rate[0]).put(rate[1],rate[2]);
			} else {
				HashMap<Integer,Integer> map = new HashMap<>();
				map.put(rate[1], rate[2]);
				userRate.put(rate[0], map);
			}
		}
		System.out.println("Done");

		try {
			PreparedStatement pStat = c
					.prepareStatement("INSERT INTO ppdata VALUES (?,?,?)");
			Statement stat = c.createStatement();
			int counter = 0;
			HashMap<Integer,Integer> uMap = null;
			String itemList = "";
			for (int[] rate : test) {
				int user = rate[0];
				int item = rate[1];
				double pred = 0;
				if (user != counter) {
					if (userRate.containsKey(user)) {
						uMap = userRate.get(user);
						itemList ="";
						for (Map.Entry<Integer, Integer> entry: uMap.entrySet()){
							itemList += ","+Integer.toString(entry.getKey());
						}
						itemList = itemList.substring(1);
					}else{
						uMap = null;
					}
					counter = user;
				}
				System.out.println(itemList);
				if (uMap != null) {
					double top = 0, bot = 0;
					ResultSet rs = stat.executeQuery("select * from (select itemI,sim from sim where itemI in ("+itemList+") and itemJ='"+item+"') where sim>=0.5");
					while(rs.next()){
						double sim = rs.getDouble(2);
						top += sim * uMap.get(rs.getInt(1));
						bot += sim;
					}
					rs = stat.executeQuery("select * from (select itemJ,sim from sim where itemI='"+item+"' and itemJ in ("+itemList+")) where sim>=0.5");
					while(rs.next()){
						double sim = rs.getDouble(2);
						top += sim*uMap.get(rs.getInt(1));
						bot += sim;
					}
					if(top != 0 && bot != 0){
						pred = top/bot;
						if(pred > 5){
							pred = 5;
						}
					}
				}
				pStat.setInt(1, user);
				pStat.setInt(2, item);
				pStat.setDouble(3, pred);
				pStat.execute();
				System.out.println(user);
				if (user % 1000 == 0) {
					c.commit();
				}
			}
			c.commit();
		} catch (Exception e) {
			cf.error(e);
		}
	}
}