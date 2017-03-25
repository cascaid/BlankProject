package sprites.characters;

import java.util.HashMap;
import java.util.HashSet;

import improbable.collections.Option;
import improbable.player.Authority;
import improbable.worker.Connection;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.View;
import improbable.worker.Ops.DeleteEntityResponse;
import improbable.worker.Ops.StatusCode;
import sprites.log.Logger;

public class Stethoscope {

	private class TrackedEntity{
		public TrackedEntity left;
		public TrackedEntity right;
		public long time;
		public EntityId entityId;
		public TrackedEntity(){}
		public TrackedEntity(EntityId entityId){
			this.entityId = entityId;
		}
		private boolean isDummy(){
			return entityId == null;
		}
	}
	private static final int timeoutMillis = 2000;

	// head <-> tail
	private TrackedEntity head = new TrackedEntity();
	private TrackedEntity tail = new TrackedEntity();
	private HashMap<EntityId, TrackedEntity> tracked = new HashMap<EntityId, TrackedEntity>();
	private HashSet<EntityId> toDelete = new HashSet<EntityId>();

	private Connection connection;

	public Stethoscope(Connection connection, View view){

		this.connection = connection;

		head.right = tail;
		tail.left = head;	

		view.onComponentUpdate(Authority.class, op -> {
			Logger.log("Heartbeat on "+op.entityId);
			if(op.update.getHeartbeat().size()>0){
				heartbeat(op.entityId);
			}
		});
		
		view.onAuthorityChange(Authority.class, op->{
			Logger.log(String.format("Have authority on %s? %s", op.entityId, op.hasAuthority));
			authorityChanged(op.entityId, op.hasAuthority);
		});

		view.onDeleteEntityResponse(op -> {
			deleteEntityReponse(op);
		});

	}

	private void heartbeat(EntityId entityId){
		TrackedEntity entity = getEntity(entityId);
		entity.time = System.currentTimeMillis();
		appendEntity(entity);
	}

	private TrackedEntity getEntity(EntityId entityId){
		if(tracked.containsKey(entityId)){
			TrackedEntity entity = tracked.get(entityId);
			entity.left.right = entity.right;
			entity.right.left = entity.left;
			return entity;
		} else {
			TrackedEntity entity = new TrackedEntity(entityId);
			tracked.put(entityId, entity);
			return entity;
		}
	}
	
	private void authorityChanged(EntityId entityId, boolean hasAuthority){
		TrackedEntity entity = getEntity(entityId);
		if(hasAuthority)
			appendEntity(entity);
		else
			tracked.remove(entityId);
			
	}

	private void appendEntity(TrackedEntity entity){
		entity.right = head.right;
		entity.left = head;
		head.right.left = entity;
		head.right = entity;		
	}

	public void trimEntities(long cutoff){
		TrackedEntity entity = head.right;
		while(!entity.isDummy())
		{
			if(entity.time < cutoff){
				entity.left.right = entity.right;
				entity.right.left = entity.left;
				tracked.remove(entity.entityId);
				commandToRemove(entity.entityId);
			} else {
				return;
			}
			entity = entity.right;
		}
	}

	private void commandToRemove(EntityId entityId){
		toDelete.add(entityId);
		connection.sendDeleteEntityRequest(entityId, Option.of(timeoutMillis));
	}

	private void deleteEntityReponse(DeleteEntityResponse op) {
		if(toDelete.contains(op.entityId)){
			toDelete.remove(op.entityId);
			if(op.statusCode.equals(StatusCode.FAILURE)){
				Logger.log(String.format("Failed to delete entity: %s, %s", op.entityId, op.message));
			} else {
				Logger.log(String.format("Successfully remove entity %s due to timeout", op.entityId));
			}
		}
	}
}
