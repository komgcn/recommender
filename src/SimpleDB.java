/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SimpleDB {
	final String connection_string = "jdbc:sqlite:comp3208-2015.db";
	public static Connection c;
	static ArrayList<int[]> ratings;
	static HashMap<Integer, Double> userAve;

	public SimpleDB() {
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
			System.out.println("Loading user average");
			rs = stat
					.executeQuery("select user,avg(rating) from traindata group by user");
			userAve = new HashMap<Integer, Double>();
			while (rs.next()) {
				userAve.put(rs.getInt(1), rs.getDouble(2));
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
		SimpleDB db = new SimpleDB();
		db.loadRatings();

		TreeMap<Integer, HashMap<Integer,Integer>> itemRate = new TreeMap<>();
		
		System.out.println("Loading item rating");
		for (int[] rate : ratings) {
			if (itemRate.containsKey(rate[1])) {
				itemRate.get(rate[1]).put(rate[0], rate[2]);
			} else {
				HashMap<Integer,Integer> map = new HashMap<>();
				map.put(rate[0], rate[2]);
				itemRate.put(rate[1], map);
			}
		}
		System.out.println("Done");
		try{
			PreparedStatement stat = c.prepareStatement("INSERT INTO SIM VALUES (?,?,?)");
			for (int i = 1; i < 30000; i++) {
				for (int j = i + 1; j < 30001; j++) {
					double top = 0, iBot = 0, jBot = 0, bot = 0, sim = 0;
					if(itemRate.containsKey(i) && itemRate.containsKey(j)){
						HashMap<Integer,Integer> iMap = itemRate.get(i);
						HashMap<Integer,Integer> jMap = itemRate.get(j);
						for (Map.Entry<Integer, Integer> entry: iMap.entrySet()) {
							int user = entry.getKey();
							if (jMap.containsKey(user)) {
								int iRate = entry.getValue();
								int jRate = jMap.get(user);
								double ave = userAve.get(user);
								top += (iRate-ave) * (jRate-ave);
								iBot += Math.pow(iRate-ave, 2);
					    		jBot += Math.pow(jRate-ave, 2);
							}
						}
					}
					bot = Math.sqrt(iBot) * Math.sqrt(jBot);
					if (top != 0 && bot != 0) {
						sim = top / bot;
					}
					stat.setInt(1, i);
					stat.setInt(2, j);
					stat.setDouble(3, sim);
					stat.execute();
				}
				System.out.println(i);
				if(i % 100 == 0){
					c.commit();
				}
			}
			c.commit();
		}catch (Exception e){
			db.error(e);
		}
	}
}*/