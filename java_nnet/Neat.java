package java_nnet;
import java.util.Arrays;

public class Neat {
    private Neuron[] inputNeurons;
    private int numInputs;
    private Neuron[] outputNeurons;
    private int numOutputs;
    private Neuron[] allNeurons;
    private int numNeurons;
    private int latestId;
    private double learningRate;
    public Neat(int inputs, int outputs, double learnRate){
        this.inputNeurons = new Neuron[inputs];
        this.numInputs = inputs;
        this.outputNeurons = new Neuron[outputs];
        this.numOutputs = outputs;
        this.numNeurons = inputs + outputs;
        this.allNeurons = new Neuron[inputs+outputs];
        this.learningRate = learnRate;
        Neuron[] empty = new Neuron[0];
        for(int i = 0; i < inputs; i++){
            this.inputNeurons[i] = new Neuron(empty, i, 0);
        }
        this.latestId = inputs;
        for(; latestId < inputs+outputs; latestId++){
            this.outputNeurons[latestId-inputs] = new Neuron(this.inputNeurons, latestId, 1);
        }
        System.arraycopy(this.inputNeurons, 0, this.allNeurons, 0, inputs);
        System.arraycopy(this.outputNeurons, 0, this.allNeurons, inputs, outputs);
    }

    public Neat(Neuron[] neurons, int latestId, int numInputs, int numOutputs, double learningRate){
        this.allNeurons = neurons;
        this.inputNeurons = new Neuron[numInputs];
        System.arraycopy(this.allNeurons, 0, this.inputNeurons, 0,  numInputs);
        this.outputNeurons = new Neuron[numOutputs];
        System.arraycopy(this.allNeurons, numInputs, this.outputNeurons, 0, numOutputs);
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numNeurons = neurons.length;
        this.learningRate = learningRate;
        this.latestId = latestId;
    }

    public Neat copy(){
        Neuron[] newNeurons = new Neuron[this.numNeurons];
        for(int i = 0; i < this.numNeurons; i++){
            newNeurons[i] = this.allNeurons[i].shallowCopy();
        }
        for (int i = this.numInputs; i < this.numNeurons; i++){
            int nInputs = this.allNeurons[i].numInputs;
            Neuron[] newConnections = new Neuron[nInputs];
            double[] newWeights = new double[nInputs];
            for (int j = 0; j < nInputs; j++){
                // Get the id of the original connected neuron.
                int connId = this.allNeurons[i].inputConnections[j].id;
                // Find its index in the original allNeurons array.
                int index = -1;
                for (int k = 0; k < this.numNeurons; k++){
                    if(this.allNeurons[k].id == connId){
                        index = k;
                        break;
                    }
                }
                if (index != -1) {
                    // Point to the new copy.
                    newConnections[j] = newNeurons[index];
                    newWeights[j] = this.allNeurons[i].connectionWeights[j];
                }
            }
            // Set the connections of the copied neuron.
            newNeurons[i].setConnections(newConnections, newWeights);
       }
       Neat newNet = new Neat(newNeurons, this.latestId, this.numInputs, this.numOutputs, this.learningRate);
       newNet.numNeurons = this.numNeurons; // Preserve the correct count.
       return newNet;
    }

    public double[] forwardPropogation(double[] inputs){
        int i;
        for(i = 0; i < this.numInputs; i++){
            this.allNeurons[i].setValue(inputs[i]);
        }
        for(; i < this.numNeurons; i++){
            this.allNeurons[i].reset();
        }
        double[] outputs = new double[this.numOutputs];
        for(i = 0; i < this.numOutputs; i++){
            outputs[i] = this.outputNeurons[i].getActivation();
        }
        return outputs;
    }

    private boolean addNeuron(){
        int[] possibleSplits = new int[this.numNeurons-this.numInputs];
        int count = 0;
        for(int i = this.numInputs; i < this.numNeurons; i++){
            if(this.allNeurons[i].numInputs > 0){
                possibleSplits[count] = i;
                count++;
            }
        }
        if(count == 0){
            return false;
        }
        int neuronIndex = possibleSplits[(int)(Math.random() * count)];
        int removedId = this.allNeurons[neuronIndex].removeRandomConnection();
        int removedIndex = 0;
        for (; removedIndex < this.numNeurons; removedIndex++){
            if (this.allNeurons[removedIndex].id == removedId){
                break;
            }
        }
        if (removedIndex == this.numNeurons){
            return false;
        }
        double layerValue = (this.allNeurons[neuronIndex].layerValue + this.allNeurons[removedIndex].layerValue)/2;
        Neuron[] temp = {this.allNeurons[removedIndex]};
        Neuron[] oldNeurons = this.allNeurons;
        this.allNeurons = new Neuron[this.numNeurons + 1];
        System.arraycopy(oldNeurons, 0, this.allNeurons, 0, this.numNeurons);
        this.allNeurons[this.numNeurons] = new Neuron(temp, this.latestId, layerValue);
        this.allNeurons[neuronIndex].addConnection(this.allNeurons[this.numNeurons]);
        this.numNeurons++;
        this.latestId++;
        return true;
    }

    private boolean removeNeuron(){
        if(this.numNeurons == this.numInputs + this.numOutputs){
            return false;
        }
        int neuron = (int) (Math.random()*(this.numNeurons-this.numInputs-this.numOutputs)) + this.numInputs + this.numOutputs;
        for(int i = this.numInputs; i < this.numNeurons; i++){
            this.allNeurons[i].removeSpecificConnection(neuron);
        }
        Neuron[] newNeurons = new Neuron[this.numNeurons-1];
        System.arraycopy(this.allNeurons, 0, newNeurons, 0, neuron);
        System.arraycopy(this.allNeurons, neuron+1, newNeurons, neuron, this.numNeurons-neuron-1);
        this.allNeurons = newNeurons;
        this.numNeurons--;
        return true;
    }

    private boolean addConnection(){
        Neuron[] sortedByLayer = Arrays.copyOfRange(this.allNeurons, this.numInputs, this.numNeurons);
        Arrays.sort(sortedByLayer, (a, b) -> Double.compare(b.layerValue, a.layerValue));
        int[][] possibleConnections = new int[this.numNeurons*(this.numNeurons-1)/2][2];
        int value = 0;
        for(int i = 0; i < sortedByLayer.length; i++){
            for(int j = i+1; j < this.numNeurons-this.numInputs; j++){
                if(!(sortedByLayer[i].containsConnection(sortedByLayer[j])) && sortedByLayer[i].layerValue > sortedByLayer[j].layerValue){
                    possibleConnections[value][0] = i;
                    possibleConnections[value][1] = j;
                    value++;
                }
            }
        }
        if(value == 0){
            return false;
        }
        value = (int) (Math.random()*value);
        sortedByLayer[possibleConnections[value][0]].addConnection(sortedByLayer[possibleConnections[value][1]]);
        return true;
    }

    private boolean removeConnection(){
        int[] possibleRemovals = new int[this.numNeurons-this.numInputs];
        int count = 0;
        for(int i = this.numInputs; i < this.numNeurons; i++){
            if(this.allNeurons[i].numInputs > 0){
                possibleRemovals[count] = i;
                count++;
            }
        }
        if(count==0){
            return false;
        }
        int connection = this.allNeurons[possibleRemovals[(int)(Math.random()*count)]].removeRandomConnection();
        while(connection == -1){
            connection = this.allNeurons[(int)(Math.random()*this.numNeurons)].removeRandomConnection();
        }
        return true;
    }

    private void changeWeights(){
        for(int i = numInputs; i < numNeurons; i++){
            this.allNeurons[i].changeWeights(this.learningRate);
        }
    }

    public void randomMutation(double[] chances){
        double randomValue = Math.random();
        if(randomValue < chances[0]){
            this.addNeuron();
        }
        else{
            randomValue -= chances[0];
            if(randomValue < chances[1]){
                boolean successful = this.removeNeuron();
                if(!successful){
                    this.randomMutation(chances);
                }
            }
            else{
                randomValue -= chances[1];
                if(randomValue < chances[2]){
                    this.addConnection();
                }
                else{
                    randomValue -= chances[2];
                    if(randomValue < chances[3]){
                        boolean successful = this.removeConnection();
                        if(!successful){
                            this.randomMutation(chances);
                        }
                    }
                    else{
                        this.changeWeights();
                    }
                }
            }
        }
        for(Neuron neuron:this.allNeurons){
            if(neuron == null){
                System.out.println("Warning - a Neuron is null");
            }
            else{
                for(Neuron inputNeuron : neuron.inputConnections){
                    if(inputNeuron == null){
                        System.out.println("Warning - an input Neuron is null");
                    }
                }
            }
        }
    }

    public void changeLearningRate(double newLearningRate){
        this.learningRate = newLearningRate;
    }
}