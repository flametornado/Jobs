package me.alex.jobs;

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
			onEntityDamageByEntity((EntityDamageByEntityEvent)event);
		}
	}
	
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		if(!event.isCancelled()){
			if(event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity){
				Player damager = (Player)event.getDamager();
				LivingEntity victim = (LivingEntity)event.getEntity();
				if(victim.getHealth() - event.getDamage() <= 0){
					// entity has been killed by a player
					if(plugin.getJob(damager) != null){
						double income = plugin.getJob(damager).getKillIncome(victim.getClass().getSimpleName().replace("Craft", ""));
						if(plugin.getiConomy() != null){
							Account account = iConomy.getBank().getAccount(damager.getName());
							account.add(income);
						}
						else if(plugin.getBOSEconomy() != null){
							plugin.getBOSEconomy().addPlayerMoney(damager.getName(), (int)income, false);
						}
					}
				}
			}
		}
	}
}
