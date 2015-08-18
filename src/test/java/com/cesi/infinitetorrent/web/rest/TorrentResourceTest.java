package com.cesi.infinitetorrent.web.rest;

import com.cesi.infinitetorrent.Application;
import com.cesi.infinitetorrent.domain.Torrent;
import com.cesi.infinitetorrent.repository.TorrentRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the TorrentResource REST controller.
 *
 * @see TorrentResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class TorrentResourceTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String DEFAULT_NAME = "SAMPLE_TEXT";
    private static final String UPDATED_NAME = "UPDATED_TEXT";
    private static final String DEFAULT_COMMENT = "SAMPLE_TEXT";
    private static final String UPDATED_COMMENT = "UPDATED_TEXT";

    private static final DateTime DEFAULT_CREATED = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_CREATED = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_CREATED_STR = dateTimeFormatter.print(DEFAULT_CREATED);
    private static final String DEFAULT_CREATED_BY = "SAMPLE_TEXT";
    private static final String UPDATED_CREATED_BY = "UPDATED_TEXT";
    private static final Long DEFAULT_TOTAL_SIZE = 0L;
    private static final Long UPDATED_TOTAL_SIZE = 0L;

    private static final byte[] DEFAULT_FILE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_FILE = TestUtil.createByteArray(2, "1");

    @Inject
    private TorrentRepository torrentRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    private MockMvc restTorrentMockMvc;

    private Torrent torrent;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TorrentResource torrentResource = new TorrentResource();
        ReflectionTestUtils.setField(torrentResource, "torrentRepository", torrentRepository);
        this.restTorrentMockMvc = MockMvcBuilders.standaloneSetup(torrentResource).setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        torrentRepository.deleteAll();
        torrent = new Torrent();
        torrent.setName(DEFAULT_NAME);
        torrent.setComment(DEFAULT_COMMENT);
        torrent.setCreated(DEFAULT_CREATED);
        torrent.setCreatedBy(DEFAULT_CREATED_BY);
        torrent.setTotalSize(DEFAULT_TOTAL_SIZE);
        torrent.setFile(DEFAULT_FILE);
    }

    @Test
    public void createTorrent() throws Exception {
        int databaseSizeBeforeCreate = torrentRepository.findAll().size();

        // Create the Torrent

        restTorrentMockMvc.perform(post("/api/torrents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(torrent)))
                .andExpect(status().isCreated());

        // Validate the Torrent in the database
        List<Torrent> torrents = torrentRepository.findAll();
        assertThat(torrents).hasSize(databaseSizeBeforeCreate + 1);
        Torrent testTorrent = torrents.get(torrents.size() - 1);
        assertThat(testTorrent.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTorrent.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testTorrent.getCreated().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_CREATED);
        assertThat(testTorrent.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testTorrent.getTotalSize()).isEqualTo(DEFAULT_TOTAL_SIZE);
        assertThat(testTorrent.getFile()).isEqualTo(DEFAULT_FILE);
    }

    @Test
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = torrentRepository.findAll().size();
        // set the field null
        torrent.setName(null);

        // Create the Torrent, which fails.

        restTorrentMockMvc.perform(post("/api/torrents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(torrent)))
                .andExpect(status().isBadRequest());

        List<Torrent> torrents = torrentRepository.findAll();
        assertThat(torrents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkCreatedIsRequired() throws Exception {
        int databaseSizeBeforeTest = torrentRepository.findAll().size();
        // set the field null
        torrent.setCreated(null);

        // Create the Torrent, which fails.

        restTorrentMockMvc.perform(post("/api/torrents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(torrent)))
                .andExpect(status().isBadRequest());

        List<Torrent> torrents = torrentRepository.findAll();
        assertThat(torrents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkCreatedByIsRequired() throws Exception {
        int databaseSizeBeforeTest = torrentRepository.findAll().size();
        // set the field null
        torrent.setCreatedBy(null);

        // Create the Torrent, which fails.

        restTorrentMockMvc.perform(post("/api/torrents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(torrent)))
                .andExpect(status().isBadRequest());

        List<Torrent> torrents = torrentRepository.findAll();
        assertThat(torrents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllTorrents() throws Exception {
        // Initialize the database
        torrentRepository.save(torrent);

        // Get all the torrents
        restTorrentMockMvc.perform(get("/api/torrents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(torrent.getId())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
                .andExpect(jsonPath("$.[*].created").value(hasItem(DEFAULT_CREATED_STR)))
                .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY.toString())))
                .andExpect(jsonPath("$.[*].totalSize").value(hasItem(DEFAULT_TOTAL_SIZE.toString())))
                .andExpect(jsonPath("$.[*].file").value(hasItem(Base64Utils.encodeToString(DEFAULT_FILE))));
    }

    @Test
    public void getTorrent() throws Exception {
        // Initialize the database
        torrentRepository.save(torrent);

        // Get the torrent
        restTorrentMockMvc.perform(get("/api/torrents/{id}", torrent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(torrent.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()))
            .andExpect(jsonPath("$.created").value(DEFAULT_CREATED_STR))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY.toString()))
            .andExpect(jsonPath("$.totalSize").value(DEFAULT_TOTAL_SIZE.toString()))
            .andExpect(jsonPath("$.file").value(Base64Utils.encodeToString(DEFAULT_FILE)));
    }

    @Test
    public void getNonExistingTorrent() throws Exception {
        // Get the torrent
        restTorrentMockMvc.perform(get("/api/torrents/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateTorrent() throws Exception {
        // Initialize the database
        torrentRepository.save(torrent);

		int databaseSizeBeforeUpdate = torrentRepository.findAll().size();

        // Update the torrent
        torrent.setName(UPDATED_NAME);
        torrent.setComment(UPDATED_COMMENT);
        torrent.setCreated(UPDATED_CREATED);
        torrent.setCreatedBy(UPDATED_CREATED_BY);
        torrent.setTotalSize(UPDATED_TOTAL_SIZE);
        torrent.setFile(UPDATED_FILE);
        

        restTorrentMockMvc.perform(put("/api/torrents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(torrent)))
                .andExpect(status().isOk());

        // Validate the Torrent in the database
        List<Torrent> torrents = torrentRepository.findAll();
        assertThat(torrents).hasSize(databaseSizeBeforeUpdate);
        Torrent testTorrent = torrents.get(torrents.size() - 1);
        assertThat(testTorrent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTorrent.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testTorrent.getCreated().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_CREATED);
        assertThat(testTorrent.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testTorrent.getTotalSize()).isEqualTo(UPDATED_TOTAL_SIZE);
        assertThat(testTorrent.getFile()).isEqualTo(UPDATED_FILE);
    }

    @Test
    public void deleteTorrent() throws Exception {
        // Initialize the database
        torrentRepository.save(torrent);

		int databaseSizeBeforeDelete = torrentRepository.findAll().size();

        // Get the torrent
        restTorrentMockMvc.perform(delete("/api/torrents/{id}", torrent.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Torrent> torrents = torrentRepository.findAll();
        assertThat(torrents).hasSize(databaseSizeBeforeDelete - 1);
    }
}