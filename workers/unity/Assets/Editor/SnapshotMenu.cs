using System.IO;
//using Assets.EntityTemplates;
using Improbable;
using Improbable.Worker;
using UnityEngine;
using JetBrains.Annotations;
using UnityEditor;
using Improbable.General;
using Improbable.Math;
using Improbable.Unity.Core.Acls;
using Improbable.Collections;
using System.Collections.Generic;
using System.Linq;

public class SnapshotMenu : MonoBehaviour
{
    private static readonly string InitialWorldSnapshotPath = Application.dataPath +
                                                              "/../../../snapshots/default.snapshot";

    [MenuItem("Improbable/Generate Snapshot Programmatically")]
    [UsedImplicitly]
    private static void GenerateSnapshotProgrammatically()
    {
        IDictionary<EntityId, SnapshotEntity> snapshotEntities = new System.Collections.Generic.Dictionary<EntityId, SnapshotEntity>();
        var currentEntityId = 1;
        
        snapshotEntities.Add(new EntityId(currentEntityId++), Spawner());

        SaveSnapshot(snapshotEntities);
    }

    private static SnapshotEntity Spawner()
    {
        var exampleEntity = new SnapshotEntity { Prefab = "Spawner" };
        
        exampleEntity.Add(new WorldTransform.Data(new WorldTransformData(new Coordinates(0, 0, 0))));
        exampleEntity.Add(new SpawnComponent.Data(new SpawnComponentData()));

        var acl = Acl.Build()
            .SetReadAccess(CommonRequirementSets.PhysicsOrVisual)
            .SetWriteAccess<WorldTransform>(CommonRequirementSets.PhysicsOnly)
            .SetWriteAccess<SpawnComponent>(CommonRequirementSets.PhysicsOnly);

        exampleEntity.SetAcl(acl);

        return exampleEntity;
    }

    private static void SaveSnapshot(IDictionary<EntityId, SnapshotEntity> snapshotEntities)
    {
        File.Delete(InitialWorldSnapshotPath);
        var maybeError = Snapshot.Save(InitialWorldSnapshotPath, snapshotEntities);

        if (maybeError.HasValue)
        {
            Debug.LogErrorFormat("Failed to generate initial world snapshot: {0}", maybeError.Value);
        }
        else
        {
            Debug.LogFormat("Successfully generated initial world snapshot at {0}", InitialWorldSnapshotPath);
        }
    }
}
