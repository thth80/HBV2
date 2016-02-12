package project.model;

public class Move {
	public int to, from, team;
	public boolean killed;
	public Move(int from, int to, int team){
		this.to = to;
		this.from = from;
		this.team = team;
		killed = false;
	}
	public Move(int from, int to, int team, boolean killed){
		this.to = to;
		this.from = from;
		this.team = team;
		this.killed = killed;
	}
	
	public void setKilledToTrue()
	{
		this.killed = true;
	}
	public int getTo()
	{
		return to;
	}
	public int getFrom()
	{
		return from;
	}
	public boolean getKilled()
	{
		return killed;
	}
	public int getTeam()
	{
		return team;
	}
}
