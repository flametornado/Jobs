package me.alex.jobs;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;

public class Job {
	
	private int level = 1;
	private int maxExp = 0;
	private int baseXp = 100;
	private double xpMultiplyer = 1.0; 
	private String job = null;
	private int experience = 0;
	private Jobs plugin = null;
	private Player player = null;
	private double increasePerLevel = 0.05;
	private double increaseExpPerLevel = 0.1;
	private HashMap<Material, Double> jobBreakPayout = null;
	private HashMap<Material, Double> jobPlacePayout = null;
	private HashMap<String, Double> jobKillPayout = null;
	private double flatRate = 1;
	
	@SuppressWarnings("unused")
	private Job(){}

	public Job(String job, int experience, int level, Jobs plugin, Player player){
		this.plugin = plugin;
		this.increaseExpPerLevel = plugin.getLevelingRate(job);
		this.increasePerLevel = plugin.getIncomeRate(job);
		this.job = job;
		this.experience = experience;
		this.level = level;
		this.player = player;
		this.jobBreakPayout = plugin.getBreakList(job);
		this.jobPlacePayout = plugin.getPlaceList(job);
		this.jobKillPayout = plugin.getKillList(job);
		this.flatRate = plugin.getFlateRatePayout();
		this.baseXp = plugin.getBaseXp();
		this.xpMultiplyer = plugin.getXpMultiplier();
		this.maxExp = (int)(baseXp * Math.pow(1+increaseExpPerLevel, level-1));
		setDisplayName(false, getTitle() + player.getDisplayName());
		updateStats();
	}
	
	public int getExperience(){
		return experience;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void stripTitle(){
		if(plugin.getDisplayLevel() != 0){
			String title = getTitle();
			String displayName = player.getDisplayName();
			String currDisplayName = displayName.replaceFirst(title, "");
			player.setDisplayName(currDisplayName);
		}
	}
	
	private void setDisplayName(boolean broadcast, String displayName){
		if(plugin.getDisplayLevel() != 0){
			if(broadcast && plugin.isBroadcasting()){
				plugin.getServer().broadcastMessage(player.getName() + " is now a " + displayName);
			}
			player.setDisplayName(displayName);
		}
	}
	
	private String getTitle(){
		String displayName = "";
		if(plugin.getDisplayLevel() != 0){
			if(plugin.getDisplayLevel() == 1 || plugin.getDisplayLevel() == 3){
				Title title = plugin.getTitle(level);
				if(title != null){
					displayName = title.getChatColor() + title.getName() + ChatColor.WHITE + " ";
				}
			}
			if(plugin.getDisplayLevel() == 2 || plugin.getDisplayLevel() == 3){
				displayName += plugin.getChatColour(job) + job + " " + ChatColor.WHITE;
			}
		}
		return displayName;
	}
	
	public double getKillIncome(String type){
		double income = flatRate;
		if(jobKillPayout.containsKey(type)){
			income = getIncome(jobKillPayout.get(type));
			increaseExperience((int)(income*xpMultiplyer));
		}
		updateMoneyStats();
		return income;
	}
	
	public double getPlaceIncome(Block block){
		double income = flatRate;
		if(jobPlacePayout.containsKey(block.getType())){
			income = getIncome(jobPlacePayout.get(block.getType()));
			increaseExperience((int)(income*xpMultiplyer));
		}
		updateMoneyStats();
		return income;
	}
	
	public double getBreakIncome(Block block){
		double income = flatRate;
		if(jobBreakPayout.containsKey(block.getType())){
			income = getIncome(jobBreakPayout.get(block.getType()));
			increaseExperience((int)(income*xpMultiplyer));
		}
		updateMoneyStats();
		return income;
	}
	
	private double getIncome(double income){
		income = ((int)((income*Math.pow((1+increasePerLevel), level-1))*100))/100.00;
		return income;
	}
	
	public void increaseExperience(int exp){
		experience += exp;
		if(experience >= maxExp){
			String oldTitle = getTitle();
			while(experience >= maxExp){
				++level;
				experience -= maxExp;
				maxExp = (int)(baseXp *Math.pow(1+increaseExpPerLevel, level-1));
			}
			player.sendMessage(ChatColor.YELLOW + "-- Job level up --" );
			boolean broadcast = false;
			if(level % 30 == 0 || level % 60 == 0 || level % 90 == 0){
				broadcast = true;
			}
			String strippedTitle = player.getDisplayName().replaceFirst(oldTitle, "");
			setDisplayName(broadcast, getTitle() + strippedTitle);
			updateStats();
		}
	}
	
	public String getJobName(){
		return job;
	}
	
	public void showStats(Player player){
		player.sendMessage("Your Job Stats");
		player.sendMessage("Job: " + job);
		player.sendMessage("Level: " + level);
		player.sendMessage("Experience: " + experience + "/" + maxExp);
		player.sendMessage("Current Paygrade:");
		if(!jobBreakPayout.isEmpty()){
			player.sendMessage("  Harvest:");
			for(Entry<Material, Double> entry: jobBreakPayout.entrySet()){
				String item = entry.getKey().name().toLowerCase();
				item = item.replace('_', ' ');
				player.sendMessage("    " + item + " : " + getIncome(jobBreakPayout.get(entry.getKey())));
			}
		}
		if(!jobPlacePayout.isEmpty()){
			player.sendMessage("  Place:");
			for(Entry<Material, Double> entry: jobPlacePayout.entrySet()){
				String item = entry.getKey().name().toLowerCase();
				item = item.replace('_', ' ');
				player.sendMessage("    " + item + " : " + getIncome(jobPlacePayout.get(entry.getKey())));
			}
		}
		if(!jobKillPayout.isEmpty()){
			player.sendMessage("  Kill:");
			for(Entry<String, Double> entry: jobKillPayout.entrySet()){
				String item = entry.getKey();
				player.sendMessage("    " + item + " : " + getIncome(jobKillPayout.get(entry.getKey())));
			}
		}
	}
	
	private void updateStats(){
		if(plugin.getStats() != null){
			if(level >= plugin.getStats().get(player.getName(), "job", job)){
				plugin.getStats().setStat(player.getName(), "job", job, level);
				plugin.getStats().saveAll();
			}
		}
	}
	
	private void updateMoneyStats(){
		if(plugin.getStats() != null){
			if(plugin.getStats().get(player.getName(), "job", "money") <= iConomy.getBank().getAccount(player.getName()).getBalance()){
				plugin.getStats().setStat(player.getName(), "job", "money", (int) iConomy.getBank().getAccount(player.getName()).getBalance());
				plugin.getStats().saveAll();
			}
		}
	}
}
