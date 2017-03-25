using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Improbable.Player;

public class Heartbeat : MonoBehaviour
{

    [Require]
    protected Authority.Writer authCheck;

    public float heartbeatInterval = 1f;

    void OnEnable()
    {
        InvokeRepeating("SendHeartbeat", 0, heartbeatInterval);
    }

    void OnDisable()
    {
        CancelInvoke("SendHeartbeat");
    }

    private void SendHeartbeat()
    {
        var heartbeat = new Authority.Update();
        heartbeat.AddHeartbeat(new Improbable.Player.Heartbeat());
        authCheck.Send(heartbeat);
    }

}
