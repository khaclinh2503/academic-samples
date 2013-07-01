// PreferentialAttachmentModel.cpp

#include "stdafx.h"
#include "PreferentialAttachmentModel.h"

PreferentialAttachmentModel::PreferentialAttachmentModel(PUNGraph graph, int newNodes, int edgesPerNewNode) : NetworkRobustnessModel(graph)
{
	// a list of all the edges
	// we need this to implement the preferential attachment procedure
	THashSet<TIntPr> edges;

	// add all the edges to the edges TVec so that we can select them randomly later
	for (TUNGraph::TEdgeI edgeI = graph->BegEI(); edgeI < graph->EndEI(); edgeI++)
		edges.AddKey(TIntPr(min(edgeI.GetSrcNId(), edgeI.GetDstNId()), max(edgeI.GetSrcNId(), edgeI.GetDstNId())));

	// add new nodes using the preferential attachment method
	for (int i = 0; i < newNodes; i++)
	{
		for (int j = 0; j < edgesPerNewNode; j++)
			AddEdge(edges, graph->AddNode(), GetRandomDestinationNodeId(edges));
	}
}

inline void PreferentialAttachmentModel::AddEdge(THashSet<TIntPr> & edges, int sourceNodeId, int destinationNodeId)
{
	TIntPr key(min(sourceNodeId, destinationNodeId), max(sourceNodeId, destinationNodeId));

	if (! edges.IsKey(key))
	{
		edges.AddKey(key);
		graph->AddEdge(sourceNodeId, destinationNodeId);
	}
}

inline TInt PreferentialAttachmentModel::GetRandomDestinationNodeId(THashSet<TIntPr> & edges)
{
	return (rand() % 2) ? edges[rand() % edges.Len()].Val1() : edges[rand() % edges.Len()].Val2();
}