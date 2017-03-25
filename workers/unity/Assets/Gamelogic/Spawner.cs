using Improbable.Unity.Core;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Improbable.General;
using Improbable.Worker;

public class Spawner : MonoBehaviour {



    void OnEnable()
    {
        Debug.Log("A Cube enabled.");
        /*
        SpatialOS.WorkerCommands.SendCommand(SpawnComponent.Commands.PlayerJoined.Descriptor,
            new PlayerJoined(SpatialOS.Configuration.WorkerId),
            gameObject.EntityId(),
            result =>
            {
                if (result.StatusCode != StatusCode.Success)
                {
                    Debug.LogError("Bootstrap spawn player command failed: " + result.ErrorMessage);
                    return;
                }
                Debug.Log("Bootstrap created a player entity with ID: " + result.Response.Value.entityId + " and success code " + result.Response.Value.entityId);
            });*/
    }
}
