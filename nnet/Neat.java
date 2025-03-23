package nnet;
import java.util.Arrays;

public class Neat {
    private Neuron[] inputNeurons;
    private int numInputs;
    private Neuron[] outputNeurons;
    private int numOutputs;
    private Neuron[] allNeurons;
    private int numNeurons;
    private int latestId;
    public Neat(int inputs, int outputs){
        // For optimization remove this.inputsNeurons and this.outputNeurons;
        this.inputNeurons = new Neuron[inputs];
        this.numInputs = inputs;
        this.outputNeurons = new Neuron[outputs];
        this.numOutputs = outputs;
        this.numNeurons = inputs + outputs;
        this.allNeurons = new Neuron[inputs+outputs];
        Neuron[] empty = new Neuron[0];
        for(int i = 0; i < inputs; i++){
            this.inputNeurons[i] = new Neuron(empty, i, 0);
        }
        this.latestId = inputs;
        for(; latestId < inputs+outputs; latestId++){
            this.outputNeurons[latestId] = new Neuron(this.inputNeurons, latestId, 1);
        }
        System.arraycopy(this.inputNeurons, 0, this.allNeurons, 0, inputs);
        System.arraycopy(this.outputNeurons, 0, this.allNeurons, inputs, outputs);
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

    private void addNeuron(){
        int neuron = this.numInputs + (int)(Math.random() * (this.numNeurons - this.numInputs));
        int removed = this.allNeurons[neuron].removeRandomConnection();
        double layerValue = (this.allNeurons[neuron].layerValue + this.allNeurons[removed].layerValue)/2;
        Neuron[] temp = new Neuron[1];
        temp[0] = this.allNeurons[removed];
        Neuron[] oldNeurons = this.allNeurons;
        this.allNeurons = new Neuron[this.numNeurons + 1];
        System.arraycopy(oldNeurons, 0, this.allNeurons, 0, this.numNeurons);
        this.allNeurons[this.numNeurons] = new Neuron(temp, this.latestId, layerValue);
        this.allNeurons[neuron].addConnection(this.allNeurons[this.numNeurons]);
        this.numNeurons++;
        this.latestId++;
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
        return true;
    }

    private void addConnection(){
        Neuron[] sortedByLayer = new Neuron[this.numNeurons];
        Arrays.sort(sortedByLayer, (a, b) -> Integer.compare(b.id, a.id));
        for(int i = 0; i < this.numNeurons; i++){
            for(int j = i; j < this.numNeurons; j++){
                
            }
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
                        // Remove connection
                    }
                    else{
                        // Change weights
                    }
                }
            }
        }
    }
}