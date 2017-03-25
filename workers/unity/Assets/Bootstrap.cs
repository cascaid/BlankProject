using Improbable;
using Improbable.Collections;
using Improbable.General;
using Improbable.Unity;
using Improbable.Unity.Configuration;
using Improbable.Unity.Core;
using Improbable.Unity.Core.EntityQueries;
using Improbable.Worker;
using UnityEngine;

// Placed on a gameobject in client scene to execute connection logic on client startup
public class Bootstrap : MonoBehaviour
{
    public WorkerConfigurationData Configuration = new WorkerConfigurationData();

    public void Start()
    {
        SpatialOS.ApplyConfiguration(Configuration);

        switch (SpatialOS.Configuration.WorkerPlatform)
        {
            case WorkerPlatform.UnityWorker:
                SpatialOS.OnDisconnected += reason => Application.Quit();

                var targetFramerate = 120;
                var fixedFramerate = 20;

                Application.targetFrameRate = targetFramerate;
                Time.fixedDeltaTime = 1.0f / fixedFramerate;
                break;
            case WorkerPlatform.UnityClient:
                SpatialOS.OnConnected += OnConnected;
                break;
        }

        SpatialOS.Connect(gameObject);
    }

    public void OnConnected()
    {
        Debug.Log("Bootstrap connected to SpatialOS.");
        Get();
    }


    public static EntityId spawner;
    void Get() {
        GetPlayerSpawnerEntityId((playerSpawnerEntityId, errorMessage) =>
        {
            if (errorMessage != null)
            {
                Debug.LogError("Bootstrap failed to get player spawner entity id: " + errorMessage);
                Failed();
                return;
            }
            if(playerSpawnerEntityId.HasValue)
                spawner = playerSpawnerEntityId.Value;
            Debug.Log("Bootstrap found player spawner with entity id: " + spawner);

            SpatialOS.WorkerCommands.SendCommand(SpawnComponent.Commands.PlayerJoined.Descriptor,
                                                 new PlayerJoined(SpatialOS.Configuration.WorkerId),
                                                 spawner,
                                                 result =>
                                                 {
                                                     if (result.StatusCode != StatusCode.Success)
                                                     {
                                                         Debug.LogError("Bootstrap spawn player command failed ("+ result.StatusCode+") : " + result.ErrorMessage);
                                                         Failed();
                                                         return;
                                                     }
                                                     Debug.Log("Bootstrap created a player entity with ID: " + result.Response.Value.entityId + " and success code " + result.StatusCode);
                                                 });
        });
    }

    private delegate void GetPlayerSpawnerEntityIdDelegate(Option<EntityId> playerSpawnerEntityId, string errorMessage);
    private void GetPlayerSpawnerEntityId(GetPlayerSpawnerEntityIdDelegate callback)
    {
        SpatialOS.WorkerCommands.SendQuery(Query.HasComponent<SpawnComponent>().ReturnOnlyEntityIds(), result =>
        {
            if (result.StatusCode != StatusCode.Success || !result.Response.HasValue)
            {
                callback(null, "Bootstrap find player spawner query failed with error: " + result.ErrorMessage);
                Failed();
                return;
            }

            var response = result.Response.Value;
            if (response.EntityCount < 1)
            {
                callback(null, "Bootstrap failed to find player spawner: no entities found with the Spawner component");
                Failed();
                return;
            }
            var playerSpawnerEntityId = response.Entities.First.Value.Key;
            callback(new Option<EntityId>(playerSpawnerEntityId), null);
        });
    }

    void Failed()
    {
        Debug.Log("Failed");
    }
}