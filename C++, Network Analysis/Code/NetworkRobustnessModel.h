// NetworkRobustnessModel.h

#include <Snap.h>

class NetworkRobustnessModel
{
protected:
	PUNGraph graph;

public:
	NetworkRobustnessModel(PUNGraph graph);

	int GetDiameter(int sampleSize);
	int GetNodes();
	int GetNodesInMxScc();

	void Failure(int numNodes);
	void Attack(int numNodes);
};