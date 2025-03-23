package nnet;

public class Neuron {
    private Neuron[] inputConnections;
    private double[] connectionWeights;
    private double bias;
    private boolean activated;
    private double value;
    protected int numInputs;
    protected int id;
    protected double layerValue;
    protected Neuron(Neuron[] inputs, int id, double layerValue){
        this.id = id;
        this.numInputs = inputs.length;
        this.connectionWeights = new double[this.numInputs];
        for(int i = 0; i < this.numInputs; i++){
            connectionWeights[i] = Math.random()*2-1;
            inputConnections[i] = inputs[i];
        }
        if(numInputs == 0){
            this.bias = 0;
        }
        else{
            this.bias = Math.random()*0.1-0.05;
        }
        this.activated = false;
    }

    protected double getActivation(){
        if(this.activated){
            return this.value;
        }
        else{
            for(int i = 0; i < this.numInputs; i++){
                this.value += inputConnections[i].getActivation();
            }
            this.value += bias;
            this.value = Nnet.sigmoid(this.value);
            this.activated = true;
            return this.value;
        }
    }

    protected void addConnection(Neuron newNeuron){
        Neuron[] oldNeurons = this.inputConnections;
        this.inputConnections = new Neuron[this.numInputs+1];
        for(int i = 0; i < this.numInputs; i++){
            this.inputConnections[i] = oldNeurons[i];
        }
        this.inputConnections[this.numInputs] = newNeuron;
        this.numInputs++;
    }

    protected int removeRandomConnection(){
        int index = (int)(Math.random() * this.numInputs);
        Neuron[] newNeurons = new Neuron[this.numInputs-1];
        int returnValue = this.inputConnections[index].id;
        System.arraycopy(this.inputConnections, 0, newNeurons, 0, index);
        System.arraycopy(this.inputConnections, index+1, newNeurons, index, this.numInputs-index-1);
        this.inputConnections = newNeurons;
        this.numInputs--;
        return returnValue;
    }

    protected boolean removeSpecificConnection(int id){
        int index;
        for(index = 0; index < this.numInputs; index++){
            if(this.inputConnections[index].id == id){
                break;
            }
        }
        if(index == this.numInputs){
            return false;
        }
        Neuron[] newNeurons = new Neuron[this.numInputs-1];
        System.arraycopy(this.inputConnections, 0, newNeurons, 0, index);
        System.arraycopy(this.inputConnections, index+1, newNeurons, index, this.numInputs-index-1);
        this.inputConnections = newNeurons;
        this.numInputs--;
        return true;
    }

    protected void setValue(double value){
        this.value = value;
        this.activated = true;
    }

    protected void reset(){
        this.activated = false;
    }
}