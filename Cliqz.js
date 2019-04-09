export default class Cliqz {
  constructor(app, actions) {
    this.app = app;
    this.app.modules['ui'] = {
      status() {
        return {
          name: 'ui',
          isEnabled: true,
          loadingTime: 0,
          loadingTimeSync: 0,
          windows: [],
          state: {},
        };
      },
      name: 'ui',
      action(action, ...args) {
        return Promise.resolve().then(() => {
          return actions[action](...args);
        });
      },
      isEnabled: true,
    };
    this.mobileCards = app.modules['mobile-cards'].background.actions;
    this.geolocation = app.modules['geolocation'].background.actions;
    this.search = app.modules['search'].background.actions;
    this.core = app.modules['core'].background.actions;
  }
}
