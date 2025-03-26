package nnet;

public class Neuron {
    protected Neuron[] inputConnections;
    protected double[] connectionWeights;
    protected double bias;
    private boolean activated;
    private double value;
    protected int numInputs;
    protected int id;
    protected double layerValue;
    protected Neuron(Neuron[] inputs, int id, double layerValue){
        this.id = id;
        this.numInputs = inputs.length;
        this.connectionWeights = new double[this.numInputs];
        this.inputConnections = new Neuron[this.numInputs];
        this.layerValue = layerValue;
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

    protected Neuron shallowCopy(){
        Neuron copy = new Neuron(new Neuron[0], this.id, this.layerValue);
        copy.bias = this.bias;
        return copy;
    }

    protected void setConnections(Neuron[] connections, double[] weights){
        this.inputConnections = connections;
        this.connectionWeights = weights;
        this.numInputs = connections.length;
    }

    protected double getActivation(){
        if(this.activated){
            return this.value;
        }
        else{
            this.value = 0;
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
        if(newNeuron.layerValue >= this.layerValue){
            System.out.println("Warning - possible circular network");
        }
        Neuron[] oldNeurons = this.inputConnections;
        double[] oldWeights = this.connectionWeights;
        this.inputConnections = new Neuron[this.numInputs + 1];
        this.connectionWeights = new double[this.numInputs + 1];
        for (int i = 0; i < this.numInputs; i++){
            this.inputConnections[i] = oldNeurons[i];
            this.connectionWeights[i] = oldWeights[i];
        }
        this.inputConnections[this.numInputs] = newNeuron;
        this.connectionWeights[this.numInputs] = Math.random() * 2 - 1;
        this.numInputs++;
    }
    
    protected int removeRandomConnection(){
        if (this.numInputs == 0) {
            return -1;
        }
        int index = (int)(Math.random() * this.numInputs);
        int returnValue = this.inputConnections[index].id;
        Neuron[] newNeurons = new Neuron[this.numInputs - 1];
        double[] newWeights = new double[this.numInputs - 1];
        System.arraycopy(this.inputConnections, 0, newNeurons, 0, index);
        System.arraycopy(this.connectionWeights, 0, newWeights, 0, index);
        System.arraycopy(this.inputConnections, index + 1, newNeurons, index, this.numInputs - index - 1);
        System.arraycopy(this.connectionWeights, index + 1, newWeights, index, this.numInputs - index - 1);
        
        this.inputConnections = newNeurons;
        this.connectionWeights = newWeights;
        this.numInputs--;
        return returnValue;
    }
    
    protected boolean removeSpecificConnection(int id){
        int index = -1;
        for (int i = 0; i < this.numInputs; i++){
            if (this.inputConnections[i].id == id){
                index = i;
                break;
            }
        }
        if (index == -1){
            return false;
        }
        Neuron[] newNeurons = new Neuron[this.numInputs - 1];
        double[] newWeights = new double[this.numInputs - 1];
        System.arraycopy(this.inputConnections, 0, newNeurons, 0, index);
        System.arraycopy(this.connectionWeights, 0, newWeights, 0, index);
        System.arraycopy(this.inputConnections, index + 1, newNeurons, index, this.numInputs - index - 1);
        System.arraycopy(this.connectionWeights, index + 1, newWeights, index, this.numInputs - index - 1);
        this.inputConnections = newNeurons;
        this.connectionWeights = newWeights;
        this.numInputs--;
        return true;
    }
    
    protected boolean containsConnection(Neuron connection){
        for(int i = 0; i < this.numInputs; i++){
            if(this.inputConnections[i] == connection){
                return true;
            }
        }
        return false;
    }

    protected void changeWeights(double learningRate){
        for(int i = 0; i < this.numInputs; i++){
            this.connectionWeights[i] += (2*Math.random()-1) * learningRate;
        }
        this.bias += (2*Math.random()-1) * learningRate;
    }

    protected void setValue(double value){
        this.value = value;
        this.activated = true;
    }

    protected void reset(){
        this.activated = false;
        this.value = 0;
    }
}