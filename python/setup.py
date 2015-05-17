from setuptools import setup

setup(
    name='geohash.py',
    version='1.0',
    packages=['geohash'],
    setup_requires=['nose>=1.0'],
    test_suite = 'nose.collector'
)
