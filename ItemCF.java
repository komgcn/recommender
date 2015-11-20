
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemCF {
	final String connection_string = "jdbc:sqlite:comp3208-2015.db";
	public static Connection c;
	static ArrayList<int[]> ratings, test;
	static HashMap<Integer,ArrayList<int[]>> userRate;

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

	public void createTable() {
		try {
			System.out.println("Creating Table");
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE temptraindata (UserID INT, ItemID INT, Rating INT)";
			stmt.executeUpdate(sql);
			c.commit();
			stmt.close();
			System.out.println("Done");
		} catch (Exception e) {
			error(e);
		}
	}

	public void addRatings(ArrayList<double[]> pred) {
		try {
			System.out.println("Inserting Data");
			PreparedStatement stat = c.prepareStatement("INSERT INTO pdata VALUES (?,?,?)");
			for (double[] rating : pred) {
				stat.setInt(1, (int) rating[0]);
				stat.setInt(2, (int) rating[1]);
				stat.setDouble(3, rating[2]);
				stat.execute();			
			}
			c.commit();
			System.out.println("Done");
		} catch (Exception e) {
			error(e);
		}
	}

	public void loadTest(){
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

	// TODO Auto-generated constructor stub
	public static void main(String[] args) {
		ItemCF cf = new ItemCF();
		cf.loadRatings();
		cf.loadTest();
		//cf.createTable();
		//cf.addRatings();
		for(int[] rate: ratings){
			if(userRate.containsKey(rate[0])){
				userRate.get(rate[0]).add(rate);
			}else{
				ArrayList<int[]> list = new ArrayList<int[]>();
				list.add(rate);
				userRate.put(rate[0], list);
			}
		}
		ArrayList<double[]> result = new ArrayList<>();
		for(int[] rate: test){
			try{
				double[] pRate = new double[3];
				pRate[0] = rate[0];
				pRate[1] = rate[1];
				Statement stat = c.createStatement();
				if(userRate.get(rate[0]) == null){
					pRate[2] = 0;
				}else{
					double top=0,bot=0,sim=0;
					for(int[] uRate : userRate.get(rate[0])){
						int i,j;
						if(uRate[1] <= rate[1]){
							i = uRate[1];
							j = rate[1];
						}else{
							i = rate[1];
							j = uRate[1];
						}
						ResultSet rs = stat.executeQuery("select * from sim where itemI='"+i+"' and itemJ='"+j+"'");
						while(rs.next()){
							sim = rs.getDouble(3);
						}
						top += sim*uRate[2];
						bot += sim;
					}
					if(top == 0 || bot == 0){
						pRate[2] = 0;
					}else{
						pRate[2] = top/bot;
					}
				}
				result.add(pRate);
			}catch(Exception e){
				cf.error(e);
			}
		}
		cf.addRatings(result);
	}
}