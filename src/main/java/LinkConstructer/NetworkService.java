package LinkConstructer;


public class NetworkService {


  public Network network;



  public NetworkService(Network network) {
    this.network = network;
  }


  public Link getLink(long startNodeId, long endNodeId) {
    return network.getLink(startNodeId, endNodeId);
  }

}