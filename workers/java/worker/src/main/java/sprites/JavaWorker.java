package sprites;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import improbable.worker.Connection;
import improbable.worker.ConnectionParameters;
import improbable.worker.CreateEntityRequest;
import improbable.worker.Entity;
import improbable.worker.IncomingCommandRequest;
import improbable.worker.NetworkConnectionType;
import improbable.worker.OpList;
import improbable.worker.Ops;
import improbable.worker.Ops.CommandRequest;
import improbable.worker.Ops.CreateEntityResponse;
import improbable.worker.Ops.ReserveEntityIdResponse;
import sprites.characters.CharacterFactory;
import sprites.characters.CharacterTracker;
import sprites.characters.Stethoscope;
import sprites.log.Logger;
import improbable.worker.RequestId;
import improbable.worker.ReserveEntityIdRequest;
import improbable.worker.View;
import improbable.ComponentAcl;
import improbable.EntityAcl;
import improbable.EntityAclData;
import improbable.WorkerAttribute;
import improbable.WorkerAttributeSet;
import improbable.WorkerRequirementSet;
import improbable.collections.Option;
import improbable.general.PlayerJoinedResponse;
import improbable.general.SpawnComponent.Commands.PlayerJoined;
import improbable.general.WorldTransform;
import improbable.general.WorldTransformData;

public class JavaWorker 
{
	public static final int millisecondsPerFrame = 1000 / 60;

	private static final int IP_INDEX = 0;
	private static final int PORT_INDEX = 1;
	private static final int API_IP_INDEX = 2;
	private static final int WORKER_ID_INDEX = 3;
	private static final int LOG_FILE_INDEX = 4;
	
	
	public static void main( String[] args )
	{
		if (args.length < 5) {
			System.exit(1);
		}

		File file = new File(args[LOG_FILE_INDEX]);

		try(FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				){
			Logger.init(bw);
			Logger.log("Logger created");
			Logger.log("Arguments:");
			for(int i=0;i<args.length;i++)
				Logger.log(args[i]);
			
			Connection connection = connect(args[IP_INDEX], args[PORT_INDEX], args[WORKER_ID_INDEX]);
			View view = new View();	

			CharacterTracker charTracker = new CharacterTracker(connection, view);
			CharacterFactory charFactory = new CharacterFactory(connection, view);
			Stethoscope stethoscope = new Stethoscope(connection, view);

			try {
				runEventLoop(connection, view, stethoscope);
			} catch (InterruptedException e) {
				Logger.log("Event Loop threw exception: "+e.getMessage());
				System.exit(1);
			}

		} catch (IOException e) {
			System.exit(1);
		}
	}

	static Connection connect(String ip, String port, String workerId){
		ConnectionParameters parameters = new ConnectionParameters();
		parameters.workerType = "JavaServer";
		parameters.workerId = workerId;
		parameters.networkParameters.type = NetworkConnectionType.Tcp;
		parameters.networkParameters.useExternalIp = false;

		return Connection.connectAsync(ip, (short)Integer.parseInt(port), parameters).get();
	}
	
	static void runEventLoop(Connection connection, View view, Stethoscope stethoscope)
			throws InterruptedException {
		int perSecondTimer = 0;
		int heartbeatTimeout = 2000;
		while (true) {
			OpList opList = connection.getOpList(0);
			view.process(opList);
			perSecondTimer++;
			perSecondTimer%=60;
			if(perSecondTimer == 0){
				long timeNow = System.currentTimeMillis();
				long cutoff = timeNow - heartbeatTimeout;
				stethoscope.trimEntities(cutoff);
			}
			Thread.sleep(millisecondsPerFrame);
		}
	}
}
