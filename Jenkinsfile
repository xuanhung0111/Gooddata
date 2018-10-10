/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

@Library('pipelines-shared-libs')
import com.gooddata.pipeline.Pipeline

def config = [
    'microservices': [
        'graphene-tests': [
            'docker': [
                'dockerfile': './Dockerfile',
                'customTags': [ '67.0.3396.79-2.39' ]
            ]
        ],
        'graphene-chrome': [
            'docker': [
                'dockerfile': './Dockerfile_chrome',
                'customTags': [ '67.0.3396.79-2.39', 'latest' ],
                'cacheFromTag': 'latest'
            ]
        ],
        'graphene-firefox': [
            'docker': [
                'dockerfile': './Dockerfile_firefox',
                'customTags': [ '60.0.1-0.20.1', 'latest' ],
                'cacheFromTag': 'latest'
            ]
        ]
    ]
]

Pipeline.get(this, config).run()
