package sprites.characters;

import java.util.HashMap;
import java.util.List;

import improbable.general.SpawnComponent.Commands.PlayerJoined;
import improbable.player.Character;
import improbable.player.Character.Commands.MoveTo;
import improbable.player.Character.Update;
import improbable.player.MovementRequest;
import improbable.player.MovementResponse;
import improbable.worker.Connection;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.IncomingCommandRequest;
import improbable.worker.Ops.CommandRequest;
import improbable.worker.RequestId;
import improbable.worker.View;
import sprites.log.Logger;

public class CharacterTracker {

	private class Point {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		public int x, y;
		Point(int x, int y){
			this.x = x;
			this.y = y;
		}
		private CharacterTracker getOuterType() {
			return CharacterTracker.this;
		}
		
	}
	
	private HashMap<EntityId, Point> entityToPoint = new HashMap<EntityId, Point>();
	private HashMap<Point, EntityId> pointToEntity = new HashMap<Point, EntityId>();

	public CharacterTracker(Connection connection, View view){

		view.onAddEntity(op -> {
			Logger.log("Adding entity "+op.prefabName+" with id "+op.entityId);
		});
		view.onDeleteEntityResponse(op -> {
			Logger.log("Delete entity "+op.entityId);
		});

		view.onCommandRequest(MoveTo.class, op->{
			handleRequest(connection, view, op);
		});
	}
	
	private void AddEntity(EntityId entityId, Entity entity){
		//TODO
	}
	private void RemoveEntity(EntityId entityId){
		//TODO
	}
	private void UpdateEntity(EntityId entityId, Point point){
		//TODO
	}
	
	private boolean isValidMove(Point point){
		return !pointToEntity.containsKey(point);
	}
	
	void handleRequest(Connection connection, View view, CommandRequest<MoveTo, MovementRequest> op){
		if(!view.entities.containsKey(op.entityId)) {
			MovementResponse response = new MovementResponse(false);
			connection.sendCommandResponse(op.requestId, response);
			return;
		}
		Entity entity = view.entities.get(op.entityId);
		Logger.log("Move on "+op.entityId+" to "+op.request.getX()+","+op.request.getY());
		Point newPoint = new Point(op.request.getX(), op.request.getY());
		if(!isValidMove(newPoint)){
			MovementResponse response = new MovementResponse(false);
			connection.sendCommandResponse(op.requestId, response);
		} else {
			Update update = new Update();
			update.setX(op.request.getX());
			update.setY(op.request.getY());
			connection.sendComponentUpdate(op.entityId, update);
			MovementResponse response = new MovementResponse(true);
			connection.sendCommandResponse(op.requestId, response);
		}
	}
	
}
