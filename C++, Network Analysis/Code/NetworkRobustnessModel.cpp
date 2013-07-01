// NetworkRobustnessModel.h

#include "stdafx.h"
#include "NetworkRobustnessModel.h"

NetworkRobustnessModel::NetworkRobustnessModel(PUNGraph graph)
{
	this->graph = graph;
}

void NetworkRobustnessModel::Failure(int numNodes)
{
	for (int i = 0; i < numNodes; i++)
	{
		graph->DelNode(graph->GetRndNI().GetId());
	}
}

void NetworkRobustnessModel::Attack(int numNodes)
{
	for (int i = 0; i < numNodes; i++)
	{
		graph->DelNode(graph->GetRndNI().GetId());
	}
}

int NetworkRobustnessModel::GetDiameter(int sampleSize)
{
	return TSnap::GetBfsFullDiam(graph, sampleSize, false);
}

int NetworkRobustnessModel::GetNodes()
{
	return graph->GetNodes();
}

int NetworkRobustnessModel::GetNodesInMxScc()
{
	return TSnap::GetMxScc(graph)->GetNodes();
}

