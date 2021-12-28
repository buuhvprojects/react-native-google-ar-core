/* eslint-disable react-native/no-inline-styles */
import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import GoogleArCoreView from 'react-native-google-ar-core';

const App = () => {
    return (
        <GoogleArCoreView style={{ flex: 1 }}>
            <View style={styles.mainContent}>
                <Text style={styles.title}>Title</Text>
            </View>
        </GoogleArCoreView>
    );
};

const styles = StyleSheet.create({
    mainContent: {
        flex: 1,
        backgroundColor: 'transparent',
        alignItems: 'center',
    },
    title: {
        fontSize: 40,
        color: '#FFF',
    },
    bottomSheet: {
        backgroundColor: '#202122',
        alignItems: 'center',
    },
    sheetTitle: {
        marginTop: 16,
        color: '#fff',
        fontSize: 26,
    },
});

export default App;
