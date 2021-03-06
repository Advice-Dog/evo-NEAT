package examples

import com.evo.NEAT.Genome
import com.evo.NEAT.InnovationCounter
import com.evo.NEAT.Pool
import com.evo.NEAT.config.Seed
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class XORTest {

    @Test
    fun `test seed 3000L`() {
        Seed.random = Random(3000L)
        InnovationCounter.reset()

        val xor = XOR()

        val pool = Pool()
        pool.initializePool()

        var topGenome: Genome
        var generation = 0

        do {
            pool.evaluateFitness(xor)
            topGenome = pool.topGenome

            pool.breedNewGeneration()
            generation++
        } while (topGenome.points < 15)

        assertEquals(502, generation)
        assertEquals(9014, InnovationCounter.innovation)
    }

    @Test
    fun `test seed 4000L`() {
        Seed.random = Random(4000L)
        InnovationCounter.reset()

        val xor = XOR()

        val pool = Pool()
        pool.initializePool()

        var topGenome: Genome
        var generation = 0

        do {
            pool.evaluateFitness(xor)
            topGenome = pool.topGenome

            pool.breedNewGeneration()
            generation++
        } while (topGenome.points < 15)

        assertEquals(788, generation)
        assertEquals(15086,InnovationCounter.innovation)
    }

    @Test
    fun `test seed 5000L`() {
        Seed.random = Random(5000L)
        InnovationCounter.reset()

        val xor = XOR()

        val pool = Pool()
        pool.initializePool()

        var topGenome: Genome
        var generation = 0

        do {
            pool.evaluateFitness(xor)
            topGenome = pool.topGenome

            pool.breedNewGeneration()
            generation++
        } while (topGenome.points < 15)

        assertEquals(517, generation)
        assertEquals(9520, InnovationCounter.innovation)
    }


}