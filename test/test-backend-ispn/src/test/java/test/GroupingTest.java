package test;

import org.infinispan.Cache;
import org.infinispan.commons.hash.Hash;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.group.Grouper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.infinispan.FenixFrameworkGrouper;
import pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd;
import pt.ist.fenixframework.backend.infinispan.InfinispanConfig;

/**
 * @author Pedro Ruivo
 * @since 2.6
 */
@RunWith(JUnit4.class)
public class GroupingTest {

    private static final int NUM_SEGMENT = 1000;
    private static final int NUM_AUTHOR = 1000;

    @Test
    public void testDummyGrouper() throws Exception {
        ConfigurationBuilderHolder holder = getDefaultConfigurations();
        ConfigurationBuilder groupingEnabled = new ConfigurationBuilder().read(holder.getDefaultConfigurationBuilder().build());
        groupingEnabled.clustering().hash().groups().enabled().addGrouper(new Grouper<String>() {
            @Override
            public String computeGroup(String key, String group) {
                return null;
            }

            @Override
            public Class<String> getKeyType() {
                return String.class;
            }
        });
        InfinispanConfig infinispanConfig = getInfinispanConfig();
        infinispanConfig.setDefaultConfiguration(groupingEnabled.build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        FenixFramework.initialize(infinispanConfig);
        Cache cache = ((InfinispanBackEnd) FenixFramework.getConfig().getBackEnd()).getInfinispanCache();
        Assert.assertTrue(cache.getCacheConfiguration().clustering().hash().groups().enabled());
        Assert.assertEquals(1, cache.getCacheConfiguration().clustering().hash().groups().groupers().size());
        Assert.assertEquals(FenixFrameworkGrouper.class, cache.getCacheConfiguration().clustering().hash().groups().groupers().get(0).getClass());
    }

    @Test
    public void testMultipleDummyGrouper() throws Exception {
        ConfigurationBuilderHolder holder = getDefaultConfigurations();
        ConfigurationBuilder groupingEnabled = new ConfigurationBuilder().read(holder.getDefaultConfigurationBuilder().build());
        groupingEnabled.clustering().hash().groups().enabled()
                .addGrouper(new Grouper<String>() {
                    @Override
                    public String computeGroup(String key, String group) {
                        return null;
                    }

                    @Override
                    public Class<String> getKeyType() {
                        return String.class;
                    }
                })
                .addGrouper(new Grouper<String>() {
                    @Override
                    public String computeGroup(String key, String group) {
                        return null;
                    }

                    @Override
                    public Class<String> getKeyType() {
                        return String.class;
                    }
                });
        InfinispanConfig infinispanConfig = getInfinispanConfig();
        infinispanConfig.setDefaultConfiguration(groupingEnabled.build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        FenixFramework.initialize(infinispanConfig);
        Cache cache = ((InfinispanBackEnd) FenixFramework.getConfig().getBackEnd()).getInfinispanCache();
        Assert.assertTrue(cache.getCacheConfiguration().clustering().hash().groups().enabled());
        Assert.assertEquals(1, cache.getCacheConfiguration().clustering().hash().groups().groupers().size());
        Assert.assertEquals(FenixFrameworkGrouper.class, cache.getCacheConfiguration().clustering().hash().groups().groupers().get(0).getClass());
    }

    @Test
    public void testGroupingEnabled() throws Exception {
        ConfigurationBuilderHolder holder = getDefaultConfigurations();
        InfinispanConfig infinispanConfig = getInfinispanConfig();
        infinispanConfig.setDefaultConfiguration(holder.getDefaultConfigurationBuilder().build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        infinispanConfig.setUseGrouping(true);
        FenixFramework.initialize(infinispanConfig);
        Cache cache = ((InfinispanBackEnd) FenixFramework.getConfig().getBackEnd()).getInfinispanCache();
        //the grouping should be enabled
        Assert.assertTrue(cache.getCacheConfiguration().clustering().hash().groups().enabled());
        Assert.assertEquals(1, cache.getCacheConfiguration().clustering().hash().groups().groupers().size());
        Assert.assertEquals(FenixFrameworkGrouper.class, cache.getCacheConfiguration().clustering().hash().groups().groupers().get(0).getClass());
    }

    @Test
    public void testGrouping() throws Exception {
        ConfigurationBuilderHolder holder = getDefaultConfigurations();
        InfinispanConfig infinispanConfig = getInfinispanConfig();
        infinispanConfig.setDefaultConfiguration(holder.getDefaultConfigurationBuilder().build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        infinispanConfig.setUseGrouping(true);
        FenixFramework.initialize(infinispanConfig);
        final Cache cache = ((InfinispanBackEnd) FenixFramework.getConfig().getBackEnd()).getInfinispanCache();

        Assert.assertTrue(cache.getCacheConfiguration().clustering().hash().groups().enabled());
        Assert.assertEquals(1, cache.getCacheConfiguration().clustering().hash().groups().groupers().size());
        Assert.assertEquals(FenixFrameworkGrouper.class, cache.getCacheConfiguration().clustering().hash().groups().groupers().get(0).getClass());

        final Hash hashFunction = cache.getCacheConfiguration().clustering().hash().hash();
        ConsistentHash ch = cache.getAdvancedCache().getDistributionManager().getConsistentHash();

        //Big hack to calculate the segments in which the authors are going to stay. copied from ispn source.
        final int age64Segment = (hashFunction.hash("64") & Integer.MAX_VALUE) / ((int) Math.ceil((double) Integer.MAX_VALUE / NUM_SEGMENT));
        final int age65Segment = (hashFunction.hash("65") & Integer.MAX_VALUE) / ((int) Math.ceil((double) Integer.MAX_VALUE / NUM_SEGMENT));

        Assert.assertFalse(age64Segment == age65Segment);

        final DomainRoot root = FenixFramework.getDomainRoot();
        final Author[] authors = new Author[NUM_AUTHOR];

        for (int i = 0; i < NUM_AUTHOR; ++i) {
            authors[i] = Author.createAuthorGroupedByAge(i, i % 2 == 0 ? 65 : 64);
            addAuthorToRoot(root, authors[i]);
        }

        for (int i = 0; i < NUM_AUTHOR; ++i) {
            final int segment = i % 2 == 0 ? age65Segment : age64Segment;
            for (String internalId : FenixFramework.getStorageKeys(authors[i])) {
                Assert.assertEquals(segment, ch.getSegment(internalId));
            }
        }
    }
    
    @Atomic
    private void addAuthorToRoot(DomainRoot root, Author author) {
	root.addTheAuthors(author);	
    }
    
    @Test
    public void testSlotColocation() throws Exception {
        ConfigurationBuilderHolder holder = getDefaultConfigurations();
        InfinispanConfig infinispanConfig = getInfinispanConfig();
        infinispanConfig.setDefaultConfiguration(holder.getDefaultConfigurationBuilder().build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        infinispanConfig.setUseGrouping(true);
        FenixFramework.initialize(infinispanConfig);
        final Cache cache = ((InfinispanBackEnd) FenixFramework.getConfig().getBackEnd()).getInfinispanCache();

        Assert.assertTrue(cache.getCacheConfiguration().clustering().hash().groups().enabled());
        Assert.assertEquals(1, cache.getCacheConfiguration().clustering().hash().groups().groupers().size());
        Assert.assertEquals(FenixFrameworkGrouper.class, cache.getCacheConfiguration().clustering().hash().groups().groupers().get(0).getClass());

        final Hash hashFunction = cache.getCacheConfiguration().clustering().hash().hash();
        ConsistentHash ch = cache.getAdvancedCache().getDistributionManager().getConsistentHash();

        final Author author = Author.createAuthor(42, 194); // Author.createAuthorGroupedByAge(42, 194);
        
        // confirm that the slots of author are co-located
        int segment = -1;
        for (String internalId : FenixFramework.getStorageKeys(author)) {
            if (segment == -1) {
        	segment = ch.getSegment(internalId);
            }
            Assert.assertEquals(segment, ch.getSegment(internalId));
        }
    }
    
    @After
    public void shutdown() {
        FenixFramework.shutdown();
    }

    private ConfigurationBuilderHolder getDefaultConfigurations() throws Exception {
        ConfigurationBuilderHolder holder = new ParserRegistry(Thread.currentThread().getContextClassLoader()).parseFile("ispn.xml");
        holder.getDefaultConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_SYNC).hash().numSegments(NUM_SEGMENT);
        return holder;
    }

    private InfinispanConfig getInfinispanConfig() {
        InfinispanConfig config = new InfinispanConfig();
        config.appNameFromString("fenix-framework-test-backend-ispn");
        return config;
    }
}
