// PreferentialAttachmentModel.h

#include "stdafx.h"
#include "NetworkRobustnessModel.h"
#include <Snap.h>

class PreferentialAttachmentModel : public NetworkRobustnessModel
{
private:
	TInt GetRandomDestinationNodeId(THashSet<TIntPr> & edges);
	void AddEdge(THashSet<TIntPr> & edges, int sourceNodeId, int destinationNodeId);

public:
	PreferentialAttachmentModel(PUNGraph graph, int newNodes, int edgesPerNewNode);
};