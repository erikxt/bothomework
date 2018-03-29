package bot.service;

import bot.entity.QaEntity;
import bot.repository.QaRepository;
import bot.repository.RedisRepository;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;

@Service
public class SearchService {

    private static Logger logger = Logger.getLogger(SearchService.class);

    @Resource
    private QaRepository qaRepository;

    @Resource
    private RedisRepository redisRepository;

    private Directory directory;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private Analyzer ikAnalyzer;

    @PostConstruct
    public void setUp() throws IOException {
        //索引存放的位置，设置在当前目录中
//        directory = FSDirectory.open(Paths.get("indexDir/"));
        directory = new RAMDirectory();
        ikAnalyzer = new IKAnalyzer();
        // 初始化IK  方便添加关键词
        Configuration configuration = DefaultConfig.getInstance();
        Dictionary.initial(configuration);
        addKeywordsToDic();
        reloadIndex();

        //创建索引的读取器
        indexReader = DirectoryReader.open(directory);
        //创建一个索引的查找器，来检索索引库
        indexSearcher = new IndexSearcher(indexReader);
    }

    private void addKeywordsToDic() {
        List<QaEntity> allQaEntities = getAllQaEntities();
        Set<String> dictSet = new HashSet<>();
        for(QaEntity entity : allQaEntities) {
            String keywords = entity.getKeywords();
            String[] keyArr = keywords.replaceAll("，", ",").split(",");
            if(keyArr != null) {
                for(String keyword : keyArr)
                    dictSet.add(keyword);
            }
        }
        Dictionary.getSingleton().addWords(dictSet);
    }

    public void reloadIndex() {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new WhitespaceAnalyzer());
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            try (IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
                initIndexs(indexWriter);
                // indexWriter.flush();
                // indexWriter.commit();
            }
        } catch (IOException e) {
            logger.error("IO error ", e);
        }
    }

    private static List<QaEntity> allQaEntity;

    /**
     * 缓存问答库
     * @return
     */
    private synchronized  List<QaEntity> getAllQaEntities() {
        if(allQaEntity == null)
            allQaEntity = qaRepository.findAll();
        return allQaEntity;
    }

    private void initIndexs(IndexWriter indexWriter) throws IOException {
        List<QaEntity> allQaEntities = getAllQaEntities();
        Map<String, Integer> map = redisRepository.getAllAccessCountMap();
        for (QaEntity qaEntity : allQaEntities) {
            Document doc = new Document();
            doc.add(new StringField("id", qaEntity.getId().toString(), Field.Store.YES));
            // 关键字用逗号分词即可  替换成一个空格方便处理
            String processKeywords = qaEntity.getKeywords().replaceAll("，", ",")
                    .replaceAll(",", " ").toLowerCase();
            // printAnalyzerDoc(processKeywords);
            doc.add(new TextField("keywords", processKeywords, Field.Store.NO));
            Integer count = map.get(qaEntity.getId().toString());
            doc.add(new IntPoint("counts", count != null ? count : 0));
            doc.add(new NumericDocValuesField("counts", count != null ? count : 0));
            // 防止重复索引
            indexWriter.updateDocument(new Term("id", qaEntity.getId()), doc);
        }
    }

    @PreDestroy
    public void tearDown() throws Exception {
        indexReader.close();
    }

    /**
     * 执行查询，并打印查询到的记录数
     *
     * @param words
     * @throws IOException
     */
    public List<String> executeQuery(String words) throws IOException {
        QueryParser queryParser = new QueryParser("keywords", ikAnalyzer);
        Query query = null;
        TopDocs topDocs = null;
        BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
                BooleanClause.Occur.FILTER};
        try {
//            query = MultiFieldQueryParser.parse(words, new String[]{"keywords", "count"}, flags, ikAnalyzer);
            query = queryParser.parse(words);
            Sort sort = new Sort(new SortField("keywords", SortField.Type.SCORE), new SortField("counts", SortField
                    .Type.INT, true));
            topDocs = indexSearcher.search(query, 5, sort);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //打印查询到的记录数
        /*logger.info("总共查询到" + topDocs.totalHits + "个文档");*/
        List<String> ret = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //取得对应的文档对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            /*logger.info("id：" + document.get("ID"));
            logger.info("" + indexSearcher.explain(query, scoreDoc.doc));
            logger.info(document.get("id") + " " + scoreDoc.score);*/
            ret.add(document.get("id"));
        }
        return ret;
    }

    /**
     * 分词打印
     *
     * @param text
     * @throws IOException
     */
    public void printAnalyzerDoc(String text) throws IOException {
        try (Analyzer an = new WhitespaceAnalyzer();
             TokenStream tokenStream = an.tokenStream("keywords", new StringReader(text))) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                logger.info(charTermAttribute.toString());
            }
            tokenStream.end();
        }
    }
}