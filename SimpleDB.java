import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class SimpleDB {
	final String connection_string = "jdbc:sqlite:comp3208-2015.db";
	public Connection c;
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

	public void addRatings(ArrayList<double[]> result) {
		try {
			System.out.println("Inserting Data");
			PreparedStatement stat = c
					.prepareStatement("INSERT INTO SIM VALUES (?,?,?)");
			for (double[] matrix : result) {
				stat.setInt(1, (int) matrix[0]);
				stat.setInt(2, (int) matrix[1]);
				stat.setDouble(3, matrix[2]);
				stat.execute();
			}
			System.out.println("Done");
			c.commit();
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
				System.out.println("1");
			} else {
				HashMap<Integer,Integer> map = new HashMap<>();
				map.put(rate[0], rate[2]);
				itemRate.put(rate[1], map);
				System.out.println("2");
			}
		}
		System.out.println("Done");
		
		for (int i = 1; i < 30000; i++) {
			for (int j = i + 1; j < 30001; j++) {
				double top = 0, iBot = 0, jBot = 0, bot = 0, sim = 0;
				if(itemRate.containsKey(i) && itemRate.containsKey(j)){
					for (Map.Entry<Integer, Integer> entry: itemRate.get(i).entrySet()) {
						int user = entry.getKey();
						System.out.println("Check for "+user+" in j list");
					    if (itemRate.get(j).containsKey(user)) {
					    	int iRate = entry.getValue();
					    	int jRate = itemRate.get(j).get(user);
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
				
				/*HashMap<Integer, int[]> itemJ = new HashMap<>();
				for (int[] J : jlist) {
					itemJ.put(J[0], J);
				}
				for (int[] iRate : itemI) {
					if (itemJ.containsKey(iRate[0])) {
						int[] jRate = itemJ.get(iRate[0]);
						double ave = userAve.get(iRate[0]);
						top += (iRate[2] - ave) * (jRate[2] - ave);
						iBot += (iRate[2] - ave) * (iRate[2] - ave);
						jBot += (jRate[2] - ave) * (jRate[2] - ave);
					}
				}
				bot = Math.sqrt(iBot) * Math.sqrt(jBot);
				if (top != 0 && bot != 0) {
					sim = top / bot;
				}
				double[] matrix = new double[3];
				matrix[0] = i;
				matrix[1] = j;
				matrix[2] = sim;
				result.add(matrix);*/
			}
			/*if (i % 100 == 0) {
				db.addRatings(result);
				result.clear();
			}*/
			System.out.println(i);
		}
		
		/*ArrayList<double[]> result = new ArrayList<>(); 
		Random rnd = new Random();
		for(int i = 81;i < 1000;i++){
			for(int j = i+1;j< 30001;j++){
				double sim = 1 + ( 5 - 1 ) * rnd.nextDouble();
				double[] mat = new double[3];
				mat[0] = i;
				mat[1] = j;
				mat[2] = sim;
				result.add(mat);
			}
			System.out.println(i);
		}
		db.addRatings(result);*/
	}
}