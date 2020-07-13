package com.evo.NEAT

import com.evo.NEAT.config.NEAT_Config

import java.util.ArrayList
import java.util.Collections

class Pool {


    var species = ArrayList<Species>()
        private set
    private var generations = 0
    private val topFitness: Float = 0.toFloat()
    private var poolStaleness = 0

    val topGenome: Genome
        get() {
            val allGenome = ArrayList<Genome>()

            for (s in species) {
                for (g in s.genomes) {
                    allGenome.add(g)
                }
            }
            Collections.sort(allGenome, Collections.reverseOrder())

            return allGenome[0]
        }

    val currentPopulation: Int
        get() {
            var p = 0
            for (s in species)
                p += s.genomes.size
            return p
        }

    fun initializePool() {

        for (i in 0 until NEAT_Config.POPULATION) {
            addToSpecies(Genome())
        }

    }

    fun addToSpecies(g: Genome) {
        for (s in species) {
            if (s.genomes.size == 0)
                continue
            val g0 = s.genomes[0]
            //		System.out.println(s.genomes.size());
            if (Genome.isSameSpecies(g, g0)) {
                s.genomes.add(g)
                return
            }
        }
        val childSpecies = Species()
        childSpecies.genomes.add(g)
        species.add(childSpecies)
    }

    fun evaluateFitness() {         //For Testing
        for (s in species)
            for (g in s.genomes) {
                var fitness = 0f
                g.fitness = 0f
                for (i in 0..1)
                    for (j in 0..1) {
                        val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                        val output = g.evaluateNetwork(inputs)
                        val expected = i xor j
                        //                  System.out.println("Inputs are " + inputs[0] +" " + inputs[1] + " output " + output[0] + " Answer : " + (i ^ j));
                        //if (output[0] == (i ^ j))
                        fitness += 1 - Math.abs(expected - output[0])
                    }
                fitness = fitness * fitness// * fitness * fitness;

                if (fitness > 15)
                    println("Fitness : $fitness")
                g.fitness = fitness
                //System.out.println("Fitness : "+fitness);
            }
        rankGlobally()
    }

    fun evaluateFitness(environment: Environment) {

        val allGenome = ArrayList<Genome>()

        for (s in species) {
            for (g in s.genomes) {
                allGenome.add(g)
            }
        }

        /*       for(int i =0; i<allGenome.size(); i++){
            for(int j = 0; j<allGenome.size(); j++){
                if(i!=j){
                    Genome player1 = allGenome.get(i);
                    Genome player2 = allGenome.get(j);
                    environment.match(player1,player2);
                }
            }
        }*/

        environment.evaluateFitness(allGenome)
        rankGlobally()
    }

    // experimental
    private fun rankGlobally() {                // set fitness to rank
        val allGenome = ArrayList<Genome>()

        for (s in species) {
            for (g in s.genomes) {
                allGenome.add(g)
            }
        }
        Collections.sort(allGenome)
        //      allGenome.get(allGenome.size()-1).writeTofile();
        //       System.out.println("TopFitness : "+ allGenome.get(allGenome.size()-1).getFitness());
        for (i in allGenome.indices) {
            allGenome[i].points = allGenome[i].fitness      //TODO use adjustedFitness and remove points
            allGenome[i].fitness = i.toFloat()
        }
    }

    // all species must have the totalAdjustedFitness calculated
    fun calculateGlobalAdjustedFitness(): Float {
        var total = 0f
        for (s in species) {
            total += s.totalAdjustedFitness
        }
        return total
    }

    fun removeWeakGenomesFromSpecies(allButOne: Boolean) {
        for (s in species) {
            s.removeWeakGenomes(allButOne)
        }
    }

    fun removeStaleSpecies() {
        val survived = ArrayList<Species>()

        if (topFitness < getTopFitness()) {
            poolStaleness = 0
        }

        for (s in species) {
            val top = s.topGenome
            if (top.fitness > s.topFitness) {
                s.topFitness = top.fitness
                s.staleness = 0
            } else {
                s.staleness = s.staleness + 1     // increment staleness
            }

            if (s.staleness < NEAT_Config.STALE_SPECIES || s.topFitness >= this.getTopFitness()) {
                survived.add(s)
            }
        }

        Collections.sort(survived, Collections.reverseOrder())

        if (poolStaleness > NEAT_Config.STALE_POOL) {
            for (i in survived.size downTo 2)
                survived.removeAt(i)
        }

        species = survived
        poolStaleness++
    }

    fun calculateGenomeAdjustedFitness() {
        for (s in species) {
            s.calculateGenomeAdjustedFitness()
        }
    }

    fun breedNewGeneration(): ArrayList<Genome> {


        calculateGenomeAdjustedFitness()
        val survived = ArrayList<Species>()

        removeWeakGenomesFromSpecies(false)
        removeStaleSpecies()
        val globalAdjustedFitness = calculateGlobalAdjustedFitness()
        val children = ArrayList<Genome>()
        var carryOver = 0f
        for (s in species) {
            val fchild =
                NEAT_Config.POPULATION * (s.totalAdjustedFitness / globalAdjustedFitness)//- 1;       // reconsider
            var nchild = fchild.toInt()
            carryOver += fchild - nchild
            if (carryOver > 1) {
                nchild++
                carryOver -= 1f
            }

            if (nchild < 1)
                continue

            survived.add(Species(s.topGenome))
            //s.removeWeakGenome(nchild);

            //children.add(s.getTopGenome());
            for (i in 1 until nchild) {
                val child = s.breedChild()
                children.add(child)
            }


        }
        species = survived
        for (child in children)
            addToSpecies(child)
        //clearInnovations();
        generations++
        return children
    }

    fun getTopFitness(): Float {
        var topFitness = 0f
        var topGenome: Genome? = null
        for (s in species) {
            topGenome = s.topGenome
            if (topGenome!!.fitness > topFitness) {
                topFitness = topGenome.fitness
            }
        }
        return topFitness
    }
}