package sprites.characters;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import improbable.ComponentAcl;
import improbable.EntityAcl;
import improbable.EntityAclData;
import improbable.WorkerAttribute;
import improbable.WorkerAttributeSet;
import improbable.WorkerRequirementSet;
import improbable.collections.Option;
import improbable.general.PlayerJoinedResponse;
import improbable.general.WorldTransform;
import improbable.general.WorldTransformData;
import improbable.general.SpawnComponent.Commands.PlayerJoined;
import improbable.worker.Connection;
import improbable.worker.CreateEntityRequest;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import improbable.worker.IncomingCommandRequest;
import improbable.worker.Ops;
import improbable.worker.RequestId;
import improbable.worker.ReserveEntityIdRequest;
import improbable.worker.View;
import improbable.worker.Ops.CommandRequest;
import improbable.worker.Ops.CreateEntityResponse;
import improbable.worker.Ops.ReserveEntityIdResponse;
import sprites.log.Logger;

public class CharacterFactory {

	private class Request {
		//We will eventually use worker ID when registering our map of workers to characters
		String workerId;
		List<String> workerFlags;
		RequestId<IncomingCommandRequest<PlayerJoined>> requestId;

		Request(String workerId, List<String> workerFlags, RequestId<IncomingCommandRequest<PlayerJoined>> requestId){
			this.workerId = workerId;
			this.workerFlags = workerFlags;
			this.requestId = requestId;
		}
	}

	private HashMap<RequestId<ReserveEntityIdRequest>, Request> entityReservations = new HashMap<RequestId<ReserveEntityIdRequest>, Request>();

	private HashMap<RequestId<CreateEntityRequest>, Request> entityCreations = new HashMap<RequestId<CreateEntityRequest>, Request>();

	public CharacterFactory(Connection connection, View view){

		view.onCommandRequest(PlayerJoined.class, op->{
			createCharacter(connection, view, op);
		});
		view.onReserveEntityIdResponse(op -> {
			entityReservation(connection, op);
		});
		view.onCreateEntityResponse(op -> {
			entityCreation(connection, op);
		});
	}	

	private void createCharacter(Connection connection, View view, CommandRequest<PlayerJoined, improbable.general.PlayerJoined> op){

		Request req = new Request(op.callerWorkerId, op.callerAttributeSet, op.requestId);
		RequestId<ReserveEntityIdRequest> entityIdReservationRequestId = connection.sendReserveEntityIdRequest(Option.of(2000));
		entityReservations.put(entityIdReservationRequestId, req);

	}

	private void entityReservation(Connection connection, ReserveEntityIdResponse op){

		if (entityReservations.containsKey(op.requestId)){
			if(op.statusCode == Ops.StatusCode.SUCCESS) {
				Request originalRequest = entityReservations.get(op.requestId);
				Entity entity = new Entity();
				entity.add(WorldTransform.class, new WorldTransformData(new improbable.math.Coordinates(1, 2, 3)));
				entity.add(improbable.player.Character.class, new improbable.player.CharacterData("Name", 0, 0));
				entity.add(improbable.player.Authority.class, new improbable.player.AuthorityData());
				setACL(originalRequest.workerFlags, entity);
				RequestId<CreateEntityRequest> entityCreationRequest = connection.sendCreateEntityRequest(entity, Option.of("Character"), op.entityId, Option.of(2000));

				entityReservations.remove(op.requestId);
				entityCreations.put(entityCreationRequest, originalRequest);
			} else {
				Logger.log("Failed to reserve an entity: "+op.message);
				Request originalRequest = entityReservations.get(op.requestId);
				PlayerJoinedResponse response = new improbable.general.PlayerJoinedResponse(null, false);
				connection.sendCommandResponse(originalRequest.requestId, response);
				entityReservations.remove(op.requestId);
			}
		}

	}

	private void entityCreation(Connection connection, CreateEntityResponse op){
		if (entityCreations.containsKey(op.requestId)){
			if(op.statusCode == Ops.StatusCode.SUCCESS) {
				Request originalRequest = entityCreations.get(op.requestId);
				PlayerJoinedResponse response = new improbable.general.PlayerJoinedResponse(op.entityId.get(), true);
				connection.sendCommandResponse(originalRequest.requestId, response);
				entityCreations.remove(op.requestId);
			} else {
				Logger.log("Failed to create a character: "+op.message);
				Request originalRequest = entityCreations.get(op.requestId);
				PlayerJoinedResponse response = new improbable.general.PlayerJoinedResponse(null, false);
				connection.sendCommandResponse(originalRequest.requestId, response);
				entityCreations.remove(op.requestId);
			}
		}
	}

	private void setACL(List<String> callerAttributeSet, Entity entity)
	{
		List<WorkerAttribute> callerWorkerAttributes = new LinkedList<>();
		for (String callerAttribute : callerAttributeSet)
		{
			callerWorkerAttributes.add(new WorkerAttribute(Option.of(callerAttribute)));
		}
		WorkerAttributeSet callerWorkerAttributeSet = new WorkerAttributeSet(callerWorkerAttributes);
		WorkerRequirementSet callerWorkerRequirementSet = new WorkerRequirementSet(Collections.singletonList(callerWorkerAttributeSet));

		WorkerAttributeSet physicsWorkerAttributeSet = new WorkerAttributeSet(Collections.singletonList(new WorkerAttribute(Option.of("physics"))));
		WorkerAttributeSet clientWorkerAttributeSet = new WorkerAttributeSet(Collections.singletonList(new WorkerAttribute(Option.of("visual"))));

		WorkerRequirementSet physicsWorkerRequirementSet = new WorkerRequirementSet(Collections.singletonList(physicsWorkerAttributeSet));

		List<WorkerAttributeSet> clientOrPhysicsAttributeSets = new LinkedList<WorkerAttributeSet>();
		clientOrPhysicsAttributeSets.add(clientWorkerAttributeSet);
		clientOrPhysicsAttributeSets.add(physicsWorkerAttributeSet);

		WorkerRequirementSet clientOrPhysicsRequirementSet = new WorkerRequirementSet(clientOrPhysicsAttributeSets);

		Map<Integer, WorkerRequirementSet> authorityMap = new HashMap<>();
		authorityMap.put(WorldTransform.COMPONENT_ID, physicsWorkerRequirementSet);
		authorityMap.put(EntityAcl.COMPONENT_ID, physicsWorkerRequirementSet);
		authorityMap.put(improbable.player.Character.COMPONENT_ID, physicsWorkerRequirementSet);
		authorityMap.put(improbable.player.Authority.COMPONENT_ID, callerWorkerRequirementSet);

		ComponentAcl componentAcl = new ComponentAcl(authorityMap);

		entity.add(EntityAcl.class, new EntityAclData(Option.of(clientOrPhysicsRequirementSet), Option.of(componentAcl)));
	}

}
