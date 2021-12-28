import React from 'react';
import { View, StyleSheet, Text, TouchableOpacity } from 'react-native';
import GoogleArCoreView, { capture } from 'react-native-google-ar-core';

const App = () => {
    const onPress = async () => {
        capture().then(() => console.log('Okay'));
    }
    return (
        <GoogleArCoreView>
            <View style={styles.mainContent}>
                <TouchableOpacity onPress={onPress}>
                    <Text style={styles.title}>Tirar foto</Text>
                </TouchableOpacity>
            </View>
        </GoogleArCoreView>
    );
};

const styles = StyleSheet.create({
    mainContent: {
        flex: 1,
        backgroundColor: 'transparent',
        alignItems: 'center',
        justifyContent: 'center',
        alignContent: 'center',
    },
    title: {
        fontSize: 40,
        color: '#000',
        alignSelf: 'center',
        backgroundColor: '#FFF',
        borderRadius: 4,
        borderWidth: 1,
        paddingLeft: 10,
        paddingRight: 10,
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
