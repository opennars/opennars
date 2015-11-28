package nars.rover.physics.external;


import nars.rover.physics.external.particles.EmitterSettings;
import nars.rover.physics.external.particles.ParticleEmitter;
import org.jbox2d.common.Vec2;

import java.util.Random;

/**
 * Jetpack or thruster with particle fx
 * 
 * @author TranquilMarmot
 */
public class Jetpack {

	public final static Random rng = new Random();


	/** Player this jetpack belongs to */
	private DynamicEntity player;
	
	/** Amount of force jetpack outputs */
	private float upwardForce = 20.0f;
	
	/** Max speed the player can go on the Y axis */
	private float maxVelocityY = 15.0f;
	
	/** Whether or not the player is using the jetpack */
	private boolean jetpackOn;
	
	/** Particle emitter for jetpack */
	private ParticleEmitter jetpackEmitter;
	
	/** How much fuel is left in this jetpack. Should always be between 0 and 100. */
	private float fuel = 100.0f;
	
	/** How fast the jetpack depletes/refuels */
	private float depleteRate = 50.0f, rechargeRate = 20.0f;
	
	/** The jetpack charges faster after the player lands on the ground */
	private boolean fastRecharging;
	
	/** How fast the jetpack fast recharges */
	private float fastRechargeRate = 75.0f;
	
	/** How much fuel is left before the jetpack starts hovering */
	private float hoverStartPercent = 30.0f;
	
	/** How fast the jetpack depletes/recharges when hovering */
	private float hoverDepleteRate = 10.0f, hoverRechargeRate = 30.0f;
	
	/** Whether or not the player is hovering (< hoverStartPercent fuel) */
	private boolean isHovering;
	
	/** Force the jetpack outputs if it's hovering */
	private float hoverForce = 10.0f;
			
	/** @param player Player owning this jetpack */
	public Jetpack(DynamicEntity player){
		this.player = player;
		jetpackOn = false;
		fastRecharging = false;
		
		jetpackEmitter = new ParticleEmitter(player.getLayer() - 1, new JetpackEmitterSettings(player));
		jetpackEmitter.deactivate();
	}
	
	/**
	 * Must be called every time the owning player's update method is called
	 * @param timeStep Amount of time passed, in seconds
	 */
	public void update(float timeStep){
		jetpackEmitter.update(timeStep);
		updateSettings();
		
		isHovering = fuel < hoverStartPercent;
		
		// apply force and drain fuel if the jetpack is on
		if(jetpackOn && fuel > 0.0f){
			applyForce(timeStep);
			if(isHovering)
				fuel -= timeStep * hoverDepleteRate;
			else
				fuel -= timeStep * depleteRate;
			fastRecharging = false;
			if(fuel <= 0.0f)
				this.disable();
		}
		
		// add fuel if jetpack is recharging
		if(fuel < 100.0f && !jetpackOn){
			if(this.fastRecharging)
				fuel += timeStep * fastRechargeRate;
			else if(this.isHovering)
				fuel += timeStep * hoverRechargeRate;
			else
				fuel += timeStep * rechargeRate;
			if(fuel > 100.0f)
				fuel = 100.0f;
		}
		
//		// recharges faster after the player hits the ground
//		if(player.getJumpSensor().numContacts() > 0)
//			fastRecharging = true;
	}
	
	private void updateSettings(){
		EmitterSettings settings = jetpackEmitter.settings;
		
		// set offset
		settings.offset = new Vec2(rng.nextFloat() * settings.xLocationVariance * -0.2f, rng.nextFloat() * settings.yLocationVariance * 0.02f);
		
		// set particle lifetime
		if(rng.nextFloat() > 0.9f){
			float low = 0.8f, high = 1.0f;
			float liveLong = rng.nextFloat();
			if(liveLong < low)
				settings.particleLifetime = low * 2.0f;
			else if(liveLong > high)
				settings.particleLifetime = high * 2.0f;
			else
				settings.particleLifetime = liveLong * 2.0f;
		} else {
			settings.particleLifetime = rng.nextFloat() * 0.75f;
		}
		
		// set particle force
		settings.particleForce = new Vec2(rng.nextBoolean() ? rng.nextFloat() * -5.0f : rng.nextFloat() * -5.0f, rng.nextFloat() * -50.0f);
	}
	
	/** @return How much fuel the jetpack has left, as a percent */
	public float remainingFuel(){ return fuel; }
	
	/**
	 * Activates the player's jetpack
	 * @param timeStep Amount of time passed, in seconds
	 */
	private void applyForce(float timeStep){
		Vec2 linVec = player.body.getLinearVelocity();
		if(linVec.y <= maxVelocityY){
			if(isHovering)
				linVec.y += timeStep * hoverForce;
			else
				linVec.y += timeStep * upwardForce;
			player.body.setLinearVelocity(linVec);
			jetpackEmitter.activate();
		}
	}
	
	/** Start using the jetpack */
	public void enable(){ 
		jetpackOn = true;
		jetpackEmitter.activate();
	}
	
	/** Stop using the jetpack */
	public void disable(){
		jetpackOn = false;
		jetpackEmitter.deactivate();
	}
	
	/** @return Whether or not the jetpack is being used */
	public boolean isEnabled(){ return jetpackOn; }
	
	/** @return Percent that jeptack starts to hover at */
	public float hoverStartPercent(){ return hoverStartPercent; }
}
