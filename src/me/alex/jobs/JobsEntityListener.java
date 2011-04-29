package me.alex.jobs;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class JobsEntityListener extends EntityListener{
	private Jobs plugin;
	
	public JobsEntityListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onEntityDamage(EntityDamageEvent event){
		if(event instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
			if(!event.isCancelled()){
				if(event2.getDamager() instanceof Player && event2.getEntity() instanceof LivingEntity){
					Player damager = (Player)event2.getDamager();
					LivingEntity victim = (LivingEntity)event2.getEntity();
					if(victim.getHealth() - event2.getDamage() <= 0 && !victim.isDead() && victim.getHealth() > 0){
						// entity has been killed by a player
						// check if near a mob spawner
						List<Entity> damagerSurround = damager.getNearbyEntities(5, 5, 5);
						List<Entity> victimSurround = victim.getNearbyEntities(5, 5, 5);
						for(Entity temp: damagerSurround){
							if (temp instanceof Block){
								if(((Block)temp).getType() == Material.MOB_SPAWNER){
									return;
								}
							}
						}
						for(Entity temp: victimSurround){
							if (temp instanceof Block){
								if(((Block)temp).getType() == Material.MOB_SPAWNER){
									return;
								}
							}
						}
						if(plugin.getJob(damager) != null){
							double income = plugin.getJob(damager).getKillIncome(victim.getClass());
							if(plugin.getiConomy() != null){
								Account account = iConomy.getBank().getAccount(damager.getName());
								account.add(income);
							}
							else if(plugin.getBOSEconomy() != null){
								plugin.getBOSEconomy().addPlayerMoney(damager.getName(), (int)income, false);
							}
						}
						victim.setHealth(0);
					}
				}
			}
		}
	}
}
