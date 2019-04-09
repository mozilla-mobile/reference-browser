/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React from 'react';
import {
  DeviceEventEmitter,
  KeyboardAvoidingView,
  Image,
  Linking,
  StyleSheet,
  Text,
  TextInput,
  TouchableHighlight,
  View,
} from 'react-native';
import Cliqz from './Cliqz';
import console from 'browser-core/build/modules/core/console';
import SearchUIVertical from 'browser-core/build/modules/mobile-cards-vertical/SearchUI';
import App from 'browser-core/build/modules/core/app';
import { Provider as CliqzProvider } from 'browser-core/build/modules/mobile-cards/cliqz';
import { Provider as ThemeProvider } from 'browser-core/build/modules/mobile-cards-vertical/withTheme';

type Props = {};
export default class instantSearch extends React.Component<Props> {
  constructor(props) {
    super(props);
    this.state = {
      text: '',
      results: [],
      config: {},
      results: {
        results: []
      }
    };
    this.textInputRef = React.createRef();
  }

  onAction = ({ module, action, args, id }) => {
    return this.loadingPromise.then(() => {
      return this.state.cliqz.app.modules[module].action(action, ...args).then((response) => {
        return response;
      });
    }).catch(e => console.error(e));
  }

  async componentWillMount() {
    const app = new App();
    global.CLIQZ = { app };
    let cliqz;
    this.loadingPromise = app.start().then(async () => {
      await app.ready();
      const config = {};//await Bridge.getConfig();
      cliqz = new Cliqz(app, this.actions);
      this.setState({
        cliqz,
        config,
      });
      app.events.sub('search:results', (results) => {
        this.setState({ results })
      });
      app.modules.search.action('startSearch', 'cliqz');
    }).catch(console.log);
    DeviceEventEmitter.addListener('action', this.onAction);
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeAllListeners();
  }

  search(text) {
    this.setState({text});
    this.state.cliqz.search.startSearch(text);
  }

  clear() {
    this.setState({text: ''});
    if (this.textInputRef.current) this.textInputRef.current.focus();
  }

  submit = () => {
    /*
     * If there is only navigate to result, it opens the url
     * in default browser, else on submit it opens the
     * cliqz serp with query in default browser.
     */
    const { results } = this.state.results;
    const query = (this.state.text || '').trim();
    if (results.length === 1 && results[0].type === 'navigate-to') {
      Linking.openURL(results[0].url);
    } else if (query){
      Linking.openURL(`https://suche.cliqz.com/#${query}`);
    }
  }

  reportError = error => {
    // should not happen
    if (!this.state.cliqz) {
      return;
    }

    this.state.cliqz.core.sendTelemetry({
      type: 'error',
      source: 'react-native',
      error: JSON.stringify(error),
    });
  }

  render() {
    const results = this.state.results.results || [];
    const meta = this.state.results.meta || {};
    const appearance = 'light';
    const hasResults = !(results.length === 0 || !this.state.cliqz || this.state.text ==='');
    return (
      <KeyboardAvoidingView style={styles.container} enabled={!hasResults}>
        <View style={styles.searchBox}>
          <View style={{flex:5}}>
            <TextInput
                autoCapitalize = 'none'
                onChangeText={this.search.bind(this)}
                onSubmitEditing={this.submit}
                placeholder="Search now"
                autoFocus={true}
                returnKeyType='search'
                style={styles.text}
                value={this.state.text}
                ref={this.textInputRef}
              />
          </View>
          {
            this.state.text !== '' && (
              <View>
                <TouchableHighlight style={styles.clear} onPress={this.clear.bind(this)}>
                  <Image source={require('./img/clear.png')} style={{width: 15, height: 15}}/>
                </TouchableHighlight>
              </View>
            )
          }
        </View>
        {
          !hasResults
          ? (
            // <View style={styles.noresult}>
            //   <Image source={require('./img/logo.png')} style={{width: 30, height: 30}}/>
            //   <Text style={styles.noresultText}>Powered by Cliqz search</Text>
            // </View>
            null
          )
          : (
            <CliqzProvider value={this.state.cliqz}>
              <ThemeProvider value={appearance}>
                <SearchUIVertical results={results} meta={meta} theme={appearance} />
              </ThemeProvider>
            </CliqzProvider>
          )
        }
        { !hasResults &&
          <View style={{position: 'absolute', left: 0, right: 0, bottom: 10}}>
            <Text style={{color: '#0078CA', textAlign: 'center'}} onPress={() => Linking.openURL('https://cliqz.com/en/privacy-browser')}>Privacy policy</Text>
          </View>}
      </KeyboardAvoidingView>
    );
  }
}

const theme = {
  dark: {
    backgroundColor: 'rgba(0, 9, 23, 0.85)',
  },
  light: {
    backgroundColor: 'rgba(243, 244, 245, 0.93)',
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.light.backgroundColor,
  },
  searchBox: {
    flexDirection:'row',
    height: 40,
    margin: 10,
    alignItems:'center',
    justifyContent:'center',
    borderWidth:1,
    borderColor:'#00B0F6',
    borderRadius:25,
    backgroundColor:"#fff",
  },
  text: {
    backgroundColor: 'transparent',
    paddingBottom: 0,
    paddingLeft: 20,
    paddingTop: 0,
  },
  noresult: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 10,
  },
  noresultText: {
    marginLeft: 5,
    marginTop: 5,
  },
  clear: {
    padding: 10,
    paddingLeft: 15,
    paddingRight: 20
  }
});
