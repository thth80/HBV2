package project.model;
import java.sql.*;
import java.text.SimpleDateFormat;
 
public class DataCenter
{
	private static DataCenter instance = new DataCenter();
	private static TrophyRow[] trophies = instance.createTheTrophyList();
	private static final String forwardStepCounter ="SELECT SUM(steps) "+
													"FROM (SELECT SUM(167-WinSteps) AS steps FROM GameResults WHERE winner='?' "
											  		+" UNION "+
													"SELECT SUM(167-lossSteps) AS steps FROM GameResults WHERE loser='?' )";
	private static final String tripleCounter = 
			 "SELECT COUNT(*) FROM GameResults WHERE winner='?' AND multiplier=3";
	private static final String doubleCounter = 
			 "SELECT COUNT(*) FROM GameResults WHERE winner='?' AND multiplier=3";
	
	private static final String ifWinrateSufficientThenPointCount = 
			"SELECT CASE WHEN CAST(winp.count as FLOAT)/(winp.count+lossp.count) > 0.5 THEN winp.count+lossp.count ELSE	0 END "+
			"FROM (SELECT SUM(points) AS count FROM GameResults WHERE winner = '?' AND loser = ''Hard Bot'') as winp, "+
			"(SELECT SUM(points) AS count FROM GameResults WHERE loser = '?' AND winner = ''Hard Bot'') as lossp";
			  
	private static final String countCenturies =
			"SELECT COUNT(*) FROM GameResults WHERE winner = '?' AND winSteps = 0 and lossSteps >= 100";
	
	private static final String countPawnsThatMadeIt = 
			"SELECT SUM(madeit) "+
			"FROM (SELECT SUM(15-winPawns) AS madeit FROM GameResults WHERE winner = '?' "+
			      "UNION  "+
			      "SELECT SUM(15-lossPawns) AS madeit FROM GameResults WHERE loser = '?')";
			  
	private static final String almostThereCounter = 
			"SELECT COUNT(*) FROM GameResults WHERE loser = '?' AND lossSteps = 1 ";
	
	private static final String counterOfCubeValues = 
			"SELECT COUNT(*) FROM GameResults WHERE (winner = '?' OR loser = '?') AND cube >= 16 ";
			  
	private static final String count10PointWins = 
			"SELECT COUNT(*) FROM GameResults WHERE winner = '?' AND points >= 3";
	
	private static final String countGameBeatenPlayers =
			"SELECT count(distinct loser) FROM GameResults WHERE winner = '?'";
	
	private static final String countMaxCleanSheetAgainstSpecific =
			"SELECT CASE WHEN lossp.count > 0 OR lossp.count is NULL or winp.count is NULL THEN 0 ELSE winp.count END "+		
		        "FROM (SELECT SUM(points) AS count FROM GameResults WHERE winner = '?' AND loser = '?') as winp, "+
		        "(SELECT SUM(points) AS count FROM GameResults  WHERE loser = '?' AND winner = '?') as lossp ";
	
	private static final String accumulatedPointCount = 
			"SELECT sum(points) FROM GameResults WHERE winner = '?'";
	
	
	public class TrophyRow
	{
		private String sql, descript, name, imageUrl;
		private int target;
		private int isAccumulated;
		
		public TrophyRow(String name, String desc, String imgUrl ,int targ, boolean isAcc) //einungis kallað þegar forrit er ræst á server
		{																	//þessi gildi(td curr = 0) eru aðeins notuð þegar notandi skráir sig fyrst inn
			this.descript = desc;
			this.name = name;
			this.target = targ;
			this.isAccumulated = (isAcc)? 1 : 0 ;
			this.imageUrl = imgUrl;
		}
		
		public void setQuery(String query)
		{
			this.sql = query;
		}
		
	}
	
	
	private DataCenter()
	{
		
	}
	//Byrjum með SELECT streng í row.sql.
	// UPDATE Trophies SET current = ( row.sql ) WHERE username = 'row.username' AND name = 'row.name '
	
	private static String createUpdateStatement(TrophyRow row, String sqlSelect, String username)
	{
		return "UPDATE Trophies SET current = ( "+sqlSelect+" ) WHERE username = ''"+username+"'' AND name = ''"+row.name+"'' ";
	}
	
    private static String replaceQuestionMarks(String trophyQuery, String username)
    {
    	return trophyQuery.replace("'?'", "''"+username+"''");
    }
    
    //INSERT INTO Trophies VALUES('username', 'name', 'descript', 0, target, 0, 'sql', 'imageUrl', isAccum) 
    private static String generateInsertForTrophyRow(TrophyRow row, String username)
    {
    	return  "INSERT INTO Trophies(username,name,descript,current,target,completed,imageUrl,isAccum )  "
    			+ "VALUES('"+username+"', '"+row.name+"', '"+row.descript+"', 0, "+row.target
    			+  ", 0, '"+row.imageUrl+"', "+row.isAccumulated+" )";
    }
    
    private static String generateUpdateForTrophyRow(TrophyRow row, String username)
    {
    	String selectSql = replaceQuestionMarks(row.sql, username);
    	String updateSql = createUpdateStatement(row, selectSql, username);
    	return "UPDATE Trophies SET sql = '"+updateSql+"' WHERE username = '"+username+"' AND name = '"+row.name+"' ";
    }
	
	private TrophyRow[] createTheTrophyList()
	{
		TrophyRow[] trophies = new TrophyRow[20];
		int pointer = 0;
		
		trophies[pointer] = new TrophyRow("Baby steps", "Make your pawns progress by 500 squares","derp.jpg" ,500, true);
		trophies[pointer++].setQuery(DataCenter.forwardStepCounter);
		
		trophies[pointer] = new TrophyRow("Bigger Baby steps", "Make your pawns progress by 1000 squares","derp.jpg" ,1000, true);
		trophies[pointer++].setQuery(DataCenter.forwardStepCounter);
		
		trophies[pointer] = new TrophyRow("Triple Double", "Win by Backgammons 10 times","derp.jpg" ,10, true);
		trophies[pointer++].setQuery(DataCenter.tripleCounter);
	
		trophies[pointer] = new TrophyRow("Double Double", "Make by Gammon 10 times","derp.jpg" ,10, true);
		trophies[pointer++].setQuery(DataCenter.doubleCounter);
		
		trophies[pointer] = new TrophyRow("Try Hard", "Get a 50% winrate or better versus Hard Bot(At least 10 points played)","derp.jpg" ,10, false);
		trophies[pointer++].setQuery(DataCenter.ifWinrateSufficientThenPointCount);
		
		trophies[pointer] = new TrophyRow("Centurion", "Win a game where your opponent was at least 100 steps behind you","derp.jpg" ,1, true);
		trophies[pointer++].setQuery(DataCenter.countCenturies);
		
		trophies[pointer] = new TrophyRow("50 centurion", "Win 5 games where your opponent was at least 100 steps behind you","derp.jpg" ,5, true);
		trophies[pointer++].setQuery(DataCenter.countCenturies);
		
		trophies[pointer] = new TrophyRow("We need a bigger cube!", "Play a game that gets doubled 4 times ","derp.jpg" ,1, false);
		trophies[pointer++].setQuery(DataCenter.counterOfCubeValues);
		
		trophies[pointer] = new TrophyRow("Double Trouble", "Win a 10+ point game","derp.jpg" ,1, false);
		trophies[pointer++].setQuery(DataCenter.count10PointWins);
		
		trophies[pointer] = new TrophyRow("A Leader Of Pawns", "Get 100 pawns to safety","derp.jpg" ,100, true);
		trophies[pointer++].setQuery(DataCenter.countPawnsThatMadeIt);
		
		trophies[pointer] = new TrophyRow("Pawn Moses", "Get 500 pawns to safety","derp.jpg" ,500, true);
		trophies[pointer++].setQuery(DataCenter.countPawnsThatMadeIt);
		
		trophies[pointer] = new TrophyRow("Sligthly Unlucky", "Lose a game with one step away from victory","derp.jpg" ,1, true);
		trophies[pointer++].setQuery(DataCenter.almostThereCounter);
		
		trophies[pointer] = new TrophyRow("Donald Duck Unlucky!", "Lose 5 games one step away from victory","derp.jpg" ,5, true);
		trophies[pointer++].setQuery(DataCenter.almostThereCounter);
		
		trophies[pointer] = new TrophyRow("Played Some", "Win a game against 5 different players","derp.jpg" ,5, true);
		trophies[pointer++].setQuery(DataCenter.countGameBeatenPlayers);
		
		trophies[pointer] = new TrophyRow("Spares Nobody", "Win a game against 15 different players","derp.jpg" ,15, true);
		trophies[pointer++].setQuery(DataCenter.countGameBeatenPlayers);
		
		trophies[pointer] = new TrophyRow("The Bully", "Win 10 points against some player before he wins a single point against you","derp.jpg" ,10, true);
		trophies[pointer++].setQuery(DataCenter.countMaxCleanSheetAgainstSpecific);
		
		trophies[pointer] = new TrophyRow("El Bulli!", "Win 20 points against some player before he wins a single point against you","derp.jpg" ,20, true);
		trophies[pointer++].setQuery(DataCenter.countMaxCleanSheetAgainstSpecific);
		
		trophies[pointer] = new TrophyRow("Not Pointless", "Win your first point","derp.jpg" ,1, true);
		trophies[pointer++].setQuery(DataCenter.accumulatedPointCount);
		
		trophies[pointer] = new TrophyRow("Point Hoarder", "Earn 50 Backgammon points","derp.jpg" ,50, true);
		trophies[pointer++].setQuery(DataCenter.accumulatedPointCount);
		
		trophies[pointer] = new TrophyRow("Fun up to a point", "Earn 500 Backgammon points","derp.jpg" ,500, true);
		trophies[pointer++].setQuery(DataCenter.accumulatedPointCount);
		
		//NOTE TO SELF: Int deiling í SQL er eins og í java
		
		return trophies;
	}
	
    public static String attemptLogIn(String username, String password) 
    {
        Statement stmt = null; ResultSet rs = null;
        
        try {
            stmt = makeConnection();
            rs = stmt.executeQuery("SELECT password FROM Users WHERE username='"+username+"'"); 
            
            if(rs.next() && password.equals(rs.getString(1))){
            	closeAll(stmt.getConnection(), stmt, rs);
            	return username;
            }
            else{
            	closeAll(stmt.getConnection(), stmt, rs);
            	return "";
            }
        }catch(Exception ignore){  
        	return null;  }
     }
   
    public static String attemptSignUp(String username, String password) 
    {
        Statement stmt = null; ResultSet rs = null;
        
        try {
            stmt = makeConnection();
            rs = stmt.executeQuery("SELECT count(*) as count FROM Users where username='"+username+"'"); 
            if(rs.next() && rs.getInt("count") == 0)
            {
            	stmt.executeUpdate("INSERT INTO Users VALUES('"+username+ "','" +password+ "', 'default.jpg', 'novice')");
            	closeAll(stmt.getConnection(), stmt, rs);
            	return username;
            }
            else
            {
            	closeAll(stmt.getConnection(), stmt, rs);
            	return "";
            }
        }catch(Exception ignore){  
        	return null; 
        	}
     }
    
    public static boolean generateVersusStatsMessages(String username) //ATH skilar aðeins versus hjá spilurum sem user hefur mætt
    {																		//hvað með þegar user spilar við nýjan spilara??? Betra að senda alltaf entryið?
    	Statement stmt = null; ResultSet rs = null; String overall = null;
    	StringStack versusData = new StringStack();
    	String getAllPlayed = "SELECT * FROM GameVersus WHERE (userOne = '"+username+"' OR userTwo = '"+username+"' ) AND userOnePoints+userTwoPoints > 0";
    	
    	try 
    	{
            stmt = makeConnection();
            rs = stmt.executeQuery(getAllPlayed);
            while(rs.next())
            	versusData.addStringEntry(rs.getString(1)+"_"+rs.getString(2)+"_"+rs.getInt(3)+"_"+rs.getInt(4)+"_"+rs.getInt(5)+"_"+rs.getInt(6));
            
            rs = stmt.executeQuery(DataCenter.generateOverallWinsAndLosses(username));
            overall = rs.getInt(1)+"_"+rs.getInt(2);
            closeAll(stmt.getConnection(), stmt, rs);
            
            String[] vsData = versusData.getAllStrings();
            Message[] vsMessages = new Message[vsData.length+1];
            for(int i = 0; i < vsData.length; i++)
            	vsMessages[i] = Message.versusEntryMessage(vsData[i], "versus");
            
            vsMessages[vsMessages.length - 1] = Message.versusEntryMessage(overall, "overall");
            UnreadMessageStorage.storeMessages(username, vsMessages);
            return true;

        }catch(Exception ignore){  
        	return false;  }
    }
    
    //Viljum skila client: name_descript_imageUrl_percent
    public static boolean generateAllTrophyMessages(String username)
    {
    	Statement stmt = null; ResultSet rs = null;
    	StringStack trophyData = new StringStack();
    	
    	try {
            stmt = makeConnection();
            rs = stmt.executeQuery("SELECT name, descript, current, target, imageUrl FROM Trophies WHERE username = '"+username+"'");
            while(rs.next())
            {
            	double ratio = (rs.getInt("current")*1.0)/(rs.getInt("target"))*100.0;
            	trophyData.addStringEntry(rs.getString("name")+"_"+rs.getString("descript")+"_"+rs.getString("imageUrl")+"_"+(int)ratio);
            }
            closeAll(stmt.getConnection(), stmt, rs);
            
            String[] tData = trophyData.getAllStrings();
            Message[] tMessages = new Message[tData.length];
            for(int i = 0; i < tData.length; i++)
            	tMessages[i] = Message.trophyDataMessage(tData[i]);
            UnreadMessageStorage.storeMessages(username, tMessages);
            return true;
            
        }catch(Exception ignore){  
        	return false;  }
    }
    
    private static String generateSingleVersusInsert(String newUser, String otherUser)
    {
    	 return "INSERT INTO GameVersus VALUES('"+newUser+"', '"+otherUser+"', 0, 0, 0, 0)";
    }
    
    public static boolean setUpNewUserVersusEntries(String username) throws Exception            
    {
    	Statement stmt = null; ResultSet rs = null;
        String otherUsersQuery = "SELECT username FROM Users WHERE username <> '"+username+"'";
    	StringStack otherUsers = new StringStack();
    	
        try 
        {
            stmt = makeConnection();
            rs = stmt.executeQuery(otherUsersQuery);
            while(rs.next())
            	otherUsers.addStringEntry(rs.getString("username"));
            
            String[] userList = otherUsers.getAllStrings();
            for(String otherUser : userList)
            	stmt.addBatch(DataCenter.generateSingleVersusInsert(username, otherUser));
            
            stmt.executeBatch();
            
            closeAll(stmt.getConnection(), stmt, rs);
            return true;
        }
        catch(Exception ignore){ 
        	ignore.getMessage(); 
        	throw ignore;
        	}
    }
    
    public static boolean setUpNewUserTrophyEntries(String username) throws Exception
    {
    	Statement stmt = null; ResultSet rs = null;
    	
        try 
        {
            stmt = makeConnection();
            for(TrophyRow row: DataCenter.trophies)
            	stmt.addBatch(DataCenter.generateInsertForTrophyRow(row, username));  
            
            stmt.executeBatch();
            
            for(TrophyRow row: DataCenter.trophies)
            	stmt.addBatch(DataCenter.generateUpdateForTrophyRow(row, username));
            stmt.executeBatch();
            
            closeAll(stmt.getConnection(), stmt, rs);
            return true;
        }
        catch(Exception ignore){ 
        	ignore.getMessage();
        	throw ignore;
        	}
    }
    
    public static void closeAll(Connection c, Statement s, ResultSet r)
    {
    	if(r != null)try {r.close();} catch(SQLException e){
			e.printStackTrace();
		}
    	if(s != null)try {s.close();} catch (SQLException e) {
			e.printStackTrace();
		}
    	if(r != null)try{c.close();} catch(SQLException e){
    		e.printStackTrace();}
    }
    
    private static String getCurrentDateTime()
    {
    	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        return sdfDate.format(now);
    }
    																				
    public static boolean storeSingleGameResults(String winner, String loser, int points, int cube, int multiplier, int winPawns,
    		int lossPawns,int winSteps, int lossSteps, int gameType) 
    {																												   
        Statement stmt = null;
        String time = DataCenter.getCurrentDateTime();
        String insert = "INSERT INTO GameResults VALUES('"+winner+"','"+loser+"',"+points+","+cube+","+multiplier+","+winPawns+","+lossPawns+
        				","+winSteps+","+lossSteps+","+gameType+",'"+time+"')";
        
        try 
        {
            stmt = makeConnection();
            stmt.executeUpdate(insert);
            closeAll(stmt.getConnection(), stmt, null);
            return true;
        }
        catch(Exception ignore){
               ignore.getMessage();
               return false;
                }
    }
    
    public static String updateGameVersus(String winner, String loser, int winnerPoints, int winPawnsSaved, int lossPawnsSaved)
    {
    	Statement stmt = null;
    	
    	String userOneWinnerUpdate = "UPDATE GameVersus SET userOnePoints = userOnePoints + "+winnerPoints+", pawnsSavedOne = pawnsSavedOne + "+winPawnsSaved+
    							", pawnsSavedTwo = pawnsSavedTwo + "+lossPawnsSaved+"  WHERE userOne = '"+winner+"' AND userTwo = '"+loser+"'";
    	String userTwoWinnerUpdate = "UPDATE GameVersus SET userTwoPoints = userTwoPoints + "+winnerPoints+", pawnsSavedTwo = pawnsSavedTwo + "+winPawnsSaved+
				", pawnsSavedOne = pawnsSavedOne + "+lossPawnsSaved+"  WHERE userTwo = '"+winner+"' AND userOne = '"+loser+"'";
    	
    	String userOneWinnerQuery = "SELECT * FROM GameVersus WHERE userOne = '"+winner+"' AND userTwo = '"+loser+"'";
    	String userTwoWinnerQuery = "SELECT * FROM GameVersus WHERE userOne = '"+loser+"' AND userTwo = '"+winner+"'";
    	
    	String checkIfWinnerIsUserOne = "SELECT COUNT(*) FROM GameVersus WHERE userOne = '"+winner+"' AND userTwo = '"+loser+"'";
    	String newVersus = null;
    	
    	try
    	{
    		stmt = makeConnection();
    		ResultSet isWinnerUserOne = stmt.executeQuery(checkIfWinnerIsUserOne);
    		
    		if(isWinnerUserOne.next() && isWinnerUserOne.getInt(1) > 0)
    		{
    			stmt.executeUpdate(userOneWinnerUpdate);
    			ResultSet rs = stmt.executeQuery(userOneWinnerQuery);
    			newVersus = rs.getString(1)+"_"+rs.getString(2)+"_"+rs.getInt(3)+"_"+rs.getInt(4)+"_"+rs.getInt(5)+"_"+rs.getInt(6);
    		}
    		else
    		{
    			stmt.executeUpdate(userTwoWinnerUpdate);
    			ResultSet rs = stmt.executeQuery(userTwoWinnerQuery);
    			newVersus = rs.getString(1)+"_"+rs.getString(2)+"_"+rs.getInt(3)+"_"+rs.getInt(4)+"_"+rs.getInt(5)+"_"+rs.getInt(6);
    		}
    		closeAll(stmt.getConnection(), stmt, null);
    		return newVersus;
    	}
    	catch(Exception e){
    		e.getMessage();
    		return null;
    	}
    }
    
    private static String generateOverallWinsAndLosses(String username)
    {
    		return "SELECT winp.sum, lossp.sum FROM" +
				   "(SELECT SUM(points) as sum FROM GameResults WHERE winner = '"+username+"' ) as winp, " +
			       "(SELECT SUM(points) as sum FROM GameResults WHERE loser = '"+username+"' ) as lossp ";
    }
    
    public static boolean sendUpdatedVersusAndOverall(String winner, String loser, String newVs)
    {
    	Statement stmt = null; String winnerData = null; String loserData = null;
    	
    	String winnerOverll = DataCenter.generateOverallWinsAndLosses(winner);
    	String loserOverll =  DataCenter.generateOverallWinsAndLosses(loser);
    	
    	try
    	{
    		stmt = makeConnection();
    		ResultSet rs = stmt.executeQuery(winnerOverll);
    		if(rs.next())
    			winnerData = rs.getInt(1)+"_"+rs.getInt(2); 
    		
    		rs = stmt.executeQuery(loserOverll);
    		if(rs.next())
    			loserData = rs.getInt(1)+"_"+rs.getInt(2); 
    		
    		closeAll(stmt.getConnection(), stmt, null);
    		
    		Message[] toWinner = {Message.versusEntryMessage(winnerData, "overall"),Message.versusEntryMessage(newVs, "versus")};
    		Message[] toLoser = {Message.versusEntryMessage(loserData, "overall"),Message.versusEntryMessage(newVs, "versus")};
    		UnreadMessageStorage.storeMessages(winner, toWinner);
    		UnreadMessageStorage.storeMessages(loser, toLoser);
    		
    		return true;
    	}
    	catch(Exception e){
    		e.getMessage();
    		return false;
    	}
    }
    
    //erum með update statement ready, sækum row.sql í þær raðir með completed = 0, username = 'username' aka notFinishedTrophies 
    
    public static boolean checkForNewTrophies(String username) throws Exception
    {
    	Statement stmt = null; 
    	StringStack userData = new StringStack();
    	StringStack updates = new StringStack();
    	
    	String updatesToRun = "SELECT sql FROM Trophies WHERE username = '"+username+"' AND completed = 0";
    	
    	String findFinishedTrophies = "SELECT name, descript, imageUrl FROM Trophies WHERE username = '"+username+"' AND completed = 0"+
    								  " AND current >= target ";
    	String changeCompletedWhereNeeded = "UPDATE Trophies SET completed = 1 WHERE completed = 0 AND current >= target"; 
    	
    	try
    	{
    		stmt = makeConnection();
            ResultSet rs = stmt.executeQuery(updatesToRun);
            while(rs.next())
            	updates.addStringEntry(rs.getString("sql"));
            	
            for(String s: updates.getAllStrings())
            	stmt.addBatch(s);
            stmt.executeBatch();
            
            rs = stmt.executeQuery(findFinishedTrophies);
            while(rs.next())
            	userData.addStringEntry(rs.getString("name")+"_"+rs.getString("descript")+"_"+rs.getString("imageUrl")+"_100");
            
            stmt.executeUpdate(changeCompletedWhereNeeded);
            closeAll(stmt.getConnection(), stmt, null);
            
            String[] newTrophyData = userData.getAllStrings();
            Message[] trophyMsgs = new Message[newTrophyData.length];
            int pointer = 0;
            for(String tData: newTrophyData)
            	trophyMsgs[pointer++] = Message.announcementMessage("trophy", tData);
            
            UnreadMessageStorage.storeMessages(username, trophyMsgs);
            //UnreadMessageStorage.storeMessage(username, Message.explainMessage("NUmber of updates:" + counter));
            //UnreadMessageStorage.storeMessage(username, Message.explainMessage("Update Ran : " + runner));

            return true;
            	
    	}
    	catch(Exception e)
    	{
    		e.getMessage();
    		throw e;
    	}
    }
    
    private static Statement makeConnection() throws ClassNotFoundException, SQLException
    {
    	Class.forName("org.sqlite.JDBC");
    	Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Aðalsteinn\\workspace\\BackGammon\\base.db");
        return conn.createStatement();
    }
    
    public static boolean storeMatchResults(String winner, String loser, int pointGoal, int winPoints, int lossPoints, int type) 
    {
    	Statement stmt = null;
    	String time = DataCenter.getCurrentDateTime();
    	String insert = "INSERT INTO MatchResults VALUES('"+winner+"','"+loser+"', "+pointGoal+","+winPoints+","+lossPoints+
    						", "+type+", '"+time+"' )";
    	try 
    	{
        	stmt = makeConnection();
            stmt.executeUpdate(insert); 
            closeAll(stmt.getConnection(), stmt, null);
            return true;
           
        }catch(Exception ignore){
               ignore.getMessage();
               return false;
                }
    }
    
    public static void sendDefaults(String username) throws Exception
    {
    	Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Addi\\workspace\\Backgammon\\base.db");
        
        try{
        	Statement stmt = conn.createStatement();
        	
        }catch(Exception e)
        {
        	
        }
    }
    
    public static void deliverUserImage(String username) throws Exception //eyða þessu ef við viljum höndla hér
    {
        Statement stmt = null; String imageUrl = null; String title = null;;
        
        try{
        	stmt = makeConnection();
        	ResultSet rs = stmt.executeQuery("SELECT imageUrl,title FROM Users WHERE username='"+username+"'");
        	if(rs.next())
        	{
        		imageUrl = rs.getString("imageUrl");
        		title = rs.getString("title");
        	}
        	closeAll(stmt.getConnection(), stmt, rs);
        }catch(Exception e)
        {
        	
        }
        UnreadMessageStorage.storeMessage(username, Message.userImageMessage(imageUrl, title));
    }
    
    public static void storeOfflineDefaults(String username, String defaultString) throws Exception
    {
    	String[] split = defaultString.split("_");
    	int clock = Integer.parseInt(split[0]);
    	int random = Integer.parseInt(split[1]);
    	int points = Integer.parseInt(split[2]);
    	
    	Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Addi\\workspace\\Backgammon\\base.db");
        
        try{
        	Statement stmt = conn.createStatement();
        	String sql = "UPDATE UserSettings SET offlineClock='" + clock+ "', "+
        				  "offlineRandom='"+random+"' WHERE username='"+username+"'";
        	stmt.executeUpdate(sql);
        	
        }catch(Exception e)
        {
        	
        }
    }
}












