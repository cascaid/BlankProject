using Improbable.Player;
using Improbable.Unity.Visualizer;
using UnityEngine;

public class CharacterRenderer : MonoBehaviour
{

    [Require]
    public Character.Reader reader;

    private RectTransform rectTransform;

    void Awake()
    {
        rectTransform = GetComponent<RectTransform>();
        transform.position = Vector3.zero;
    }

    void OnEnable()
    {
        transform.parent = GameWorld.instance.transform;
        SetPosition(reader.Data.x, reader.Data.y);
        reader.ComponentUpdated.Add(OnCharacterUpdated);
    }
    void OnDisable()
    {
        transform.parent = null;
        GameWorld.instance.Remove(transform);
        reader.ComponentUpdated.Remove(OnCharacterUpdated);
    }

    private void OnCharacterUpdated(Character.Update update)
    {
        int x = update.x.HasValue ? update.x.Value : reader.Data.x;
        int y = update.y.HasValue ? update.y.Value : reader.Data.y;
        SetPosition(x,y);
    }

    private void SetPosition(int x, int y)
    {
        rectTransform.SetInsetAndSizeFromParentEdge(RectTransform.Edge.Left, GameWorld.instance.size * x, GameWorld.instance.size);
        rectTransform.SetInsetAndSizeFromParentEdge(RectTransform.Edge.Top, GameWorld.instance.size * y, GameWorld.instance.size);
        GameWorld.instance.NotifyPosition(x, y, transform);
    }
}
